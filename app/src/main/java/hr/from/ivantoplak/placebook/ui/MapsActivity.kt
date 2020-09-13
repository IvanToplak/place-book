package hr.from.ivantoplak.placebook.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import hr.from.ivantoplak.placebook.R
import hr.from.ivantoplak.placebook.adapter.BookmarkInfoWindowAdapter
import hr.from.ivantoplak.placebook.adapter.BookmarkListAdapter
import hr.from.ivantoplak.placebook.adapter.BookmarkListAdapter.BookmarkListAdapterListener
import hr.from.ivantoplak.placebook.extensions.*
import hr.from.ivantoplak.placebook.model.BookmarkView
import hr.from.ivantoplak.placebook.model.PlaceInfo
import hr.from.ivantoplak.placebook.viewmodel.MapsViewModel
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.drawer_view_maps.*
import kotlinx.android.synthetic.main.main_view_maps.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.scope.ScopeActivity
import org.koin.android.viewmodel.ext.android.viewModel

private const val REQUEST_LOCATION = 1
private const val TAG = "MapsActivity"
const val EXTRA_BOOKMARK_ID = "EXTRA_BOOKMARK_ID"
const val BUNDLE_BOOKMARK_ID = "BUNDLE_BOOKMARK_ID"
private const val AUTOCOMPLETE_REQUEST_CODE = 2

class MapsActivity : ScopeActivity(), OnMapReadyCallback, BookmarkListAdapterListener {

    private val mapsViewModel: MapsViewModel by viewModel()
    private val bookmarkInfoWindowAdapter: BookmarkInfoWindowAdapter by inject()

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient
    private lateinit var bookmarkListAdapter: BookmarkListAdapter
    private var markers = HashMap<Long, Marker>()
    private val placeFields = listOf(
        Place.Field.ID,
        Place.Field.NAME,
        Place.Field.PHONE_NUMBER,
        Place.Field.PHOTO_METADATAS,
        Place.Field.LAT_LNG,
        Place.Field.ADDRESS,
        Place.Field.TYPES
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupToolbar()
        setupLocationClient()
        setupPlacesClient()
        setupNavigationDrawer()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setupMapListeners()
        createBookmarkObserver()
        getCurrentLocation()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Log.e(TAG, "Location permission denied")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AUTOCOMPLETE_REQUEST_CODE ->
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val place = Autocomplete.getPlaceFromIntent(data)
                    val location = Location("").apply {
                        latitude = place.latLng?.latitude ?: 0.0
                        longitude = place.latLng?.longitude ?: 0.0
                    }
                    updateMapToLocation(location)
                    showProgress()
                    displayPoiGetPhotoStep(place)
                }
        }
    }

    override fun onMoveToBookmark(bookmark: BookmarkView) {
        drawerLayout.closeDrawer(drawerView)
        val marker = markers[bookmark.id]
        marker?.showInfoWindow()
        val location = Location("").apply {
            latitude = bookmark.location.latitude
            longitude = bookmark.location.longitude
        }
        updateMapToLocation(location)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )
        toggle.syncState()
    }

    private fun setupMapListeners() {
        map.setInfoWindowAdapter(bookmarkInfoWindowAdapter)
        map.setOnPoiClickListener { displayPoi(it) }
        map.setOnInfoWindowClickListener { handleInfoWindowClick(it) }
        fab.setOnClickListener { searchAtCurrentLocation() }
        map.setOnMapLongClickListener { latLng -> newBookmark(latLng) }
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun setupPlacesClient() {
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION
        )
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermissions()
        } else {
            map.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnCompleteListener {
                val location = it.result
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    val update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
                    map.moveCamera(update)
                } else {
                    Log.e(TAG, "No location found")
                }
            }
        }
    }

    private fun displayPoi(pointOfInterest: PointOfInterest) {
        showProgress()
        displayPoiGetPlaceStep(pointOfInterest)
    }

    private fun displayPoiGetPlaceStep(pointOfInterest: PointOfInterest) {
        val placeId = pointOfInterest.placeId

        val request = FetchPlaceRequest
            .builder(placeId, placeFields)
            .build()

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                displayPoiGetPhotoStep(place)
            }.addOnFailureListener { exception ->
                if (exception is ApiException) {
                    val statusCode = exception.statusCode
                    Log.e(
                        TAG,
                        "Place not found: " +
                                exception.message + ", " +
                                "statusCode: " + statusCode
                    )
                    hideProgress()
                }
            }
    }

    private fun displayPoiGetPhotoStep(place: Place) {
        val photoMetadata = place.photoMetadatas?.get(0)
        if (photoMetadata == null) {
            displayPoiDisplayStep(place, null)
            return
        }
        val photoRequest = FetchPhotoRequest.builder(photoMetadata)
            .setMaxWidth(resources.getDimensionPixelSize(R.dimen.default_image_width))
            .setMaxHeight(resources.getDimensionPixelSize(R.dimen.default_image_height))
            .build()

        placesClient.fetchPhoto(photoRequest)
            .addOnSuccessListener { fetchPhotoResponse ->
                val bitmap = fetchPhotoResponse.bitmap
                displayPoiDisplayStep(place, bitmap)
            }.addOnFailureListener { exception ->
                if (exception is ApiException) {
                    val statusCode = exception.statusCode
                    Log.e(TAG, "Place not found: ${exception.message}, statusCode: $statusCode")
                }
                hideProgress()
            }
    }

    private fun displayPoiDisplayStep(place: Place, photo: Bitmap?) {
        hideProgress()
        map.addMarker(
            place.latLng?.let {
                MarkerOptions()
                    .position(it)
                    .title(place.name)
                    .snippet(place.phoneNumber)
            }
        )?.apply { tag = PlaceInfo(place, photo) }
            ?.showInfoWindow()
    }

    private fun handleInfoWindowClick(marker: Marker) {
        marker.tag?.let { tag ->
            when (tag) {
                is PlaceInfo -> {
                    tag.place?.let {
                        lifecycle.coroutineScope.launch(Dispatchers.IO) {
                            mapsViewModel.addBookmarkFromPlace(tag.place, tag.image)
                        }
                    }
                    marker.remove()
                }
                is BookmarkView -> {
                    marker.hideInfoWindow()
                    startBookmarkDetails(tag.id)
                }
            }
        }
    }

    private fun addPlaceMarker(bookmark: BookmarkView): Marker? {
        val marker = map.addMarker(
            MarkerOptions()
                .position(bookmark.location)
                .title(bookmark.name)
                .snippet(bookmark.phone)
                .icon(BitmapDescriptorFactory.fromResource(bookmark.categoryResourceId))
                .alpha(0.8f)
        )
        marker.tag = bookmark
        markers[bookmark.id] = marker
        return marker
    }

    private fun displayAllBookmarks(bookmarks: List<BookmarkView>) {
        for (bookmark in bookmarks) {
            addPlaceMarker(bookmark)
        }
    }

    private fun createBookmarkObserver() {
        mapsViewModel.getBookmarkViews()?.observe(this, {
            map.clear()
            markers.clear()
            it?.let {
                displayAllBookmarks(it)
                bookmarkListAdapter.setBookmarkData(it)
            }
        })
    }

    private fun startBookmarkDetails(bookmarkId: Long) {
        val bundle = Bundle().apply { putLong(EXTRA_BOOKMARK_ID, bookmarkId) }
        startActivity<BookmarkDetailsActivity>(BUNDLE_BOOKMARK_ID, bundle)
    }

    private fun setupNavigationDrawer() {
        val layoutManager = LinearLayoutManager(this)
        bookmarkRecyclerView.layoutManager = layoutManager
        bookmarkListAdapter = BookmarkListAdapter(mutableListOf(), this)
        bookmarkRecyclerView.adapter = bookmarkListAdapter
    }

    private fun updateMapToLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f))
    }

    private fun searchAtCurrentLocation() {
        val bounds = RectangularBounds.newInstance(map.projection.visibleRegion.latLngBounds)
        try {
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, placeFields)
                .setLocationBias(bounds)
                .build(this)
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        } catch (e: GooglePlayServicesRepairableException) {
        } catch (e: GooglePlayServicesNotAvailableException) {
        }
    }

    private fun newBookmark(latLng: LatLng) {
        lifecycle.coroutineScope.launch(Dispatchers.IO) {
            val bookmark = mapsViewModel.addBookmark(latLng)
            bookmark?.let { startBookmarkDetails(bookmark.id) }
        }
    }

    private fun showProgress() {
        progressBar.show()
        disableUserInteraction()
    }

    private fun hideProgress() {
        progressBar.hide()
        enableUserInteraction()
    }
}
