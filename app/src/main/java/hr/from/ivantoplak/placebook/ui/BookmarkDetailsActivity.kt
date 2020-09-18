package hr.from.ivantoplak.placebook.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.coroutineScope
import hr.from.ivantoplak.placebook.R
import hr.from.ivantoplak.placebook.model.BookmarkDetailsViewData
import hr.from.ivantoplak.placebook.util.image.createUniqueImageFile
import hr.from.ivantoplak.placebook.util.image.decodeFileToSize
import hr.from.ivantoplak.placebook.util.image.decodeUriStreamToSize
import hr.from.ivantoplak.placebook.util.ui.MessageProvider
import hr.from.ivantoplak.placebook.viewmodel.BookmarkDetailsViewModel
import kotlinx.android.synthetic.main.activity_bookmark_details.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.File
import java.io.IOException
import java.net.URLEncoder

private const val TAG = "BookmarkDetailsActivity"
private const val REQUEST_CAPTURE_IMAGE = 1
private const val REQUEST_GALLERY_IMAGE = 2
private const val UPDATE_BOOKMARK_ERROR_MESSAGE = "Error occurred while updating a bookmark."
private const val DELETE_BOOKMARK_ERROR_MESSAGE = "Error occurred while deleting a bookmark."

class BookmarkDetailsActivity : AppCompatActivity(),
    PhotoOptionDialogFragment.PhotoOptionDialogListener {

    private val bookmarkDetailsViewModel: BookmarkDetailsViewModel by viewModel()
    private val messageProvider: MessageProvider by inject()

    private var bookmarkDetailsViewData: BookmarkDetailsViewData? = null
    private var photoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark_details)

        setupToolbar()
        setupFab()
        getIntentData()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_bookmark_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                saveChanges()
                true
            }
            R.id.action_delete -> {
                deleteBookmark()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
    }

    private fun populateFields() {
        bookmarkDetailsViewData?.let { bookmarkView ->
            editTextName.setText(bookmarkView.name)
            editTextPhone.setText(bookmarkView.phone)
            editTextNotes.setText(bookmarkView.notes)
            editTextAddress.setText(bookmarkView.address)
        }
    }

    private fun populateImageView() {
        bookmarkDetailsViewData?.let { bookmarkView ->
            val placeImage = bookmarkDetailsViewModel.getImage(bookmarkView.id)
            placeImage?.let {
                imageViewPlace.setImageBitmap(it)
            }
            imageViewPlace.setOnClickListener {
                replaceImage()
            }
        }
    }

    private fun getIntentData() {
        val bundle = intent.getBundleExtra(BUNDLE_BOOKMARK_ID)
        val bookmarkId = bundle?.getLong(EXTRA_BOOKMARK_ID, 0L) ?: 0L

        lifecycle.coroutineScope.launch {
            bookmarkDetailsViewModel.getBookmark(bookmarkId)?.collect { bookmarkData ->
                bookmarkDetailsViewData = bookmarkData
                populateFields()
                populateImageView()
                populateCategoryList()
            }
        }
    }

    private fun saveChanges() {
        if (editTextName.text.isNullOrBlank()) return
        bookmarkDetailsViewData?.let { bookmarkView ->
            lifecycle.coroutineScope.launch {
                val result = runCatching {
                    val updatedBookmarkView = bookmarkView.copy(
                        name = editTextName.text.toString(),
                        notes = editTextNotes.text.toString(),
                        address = editTextAddress.text.toString(),
                        phone = editTextPhone.text.toString(),
                        category = spinnerCategory.selectedItem as String
                    )

                    bookmarkDetailsViewModel.updateBookmark(updatedBookmarkView)
                }
                result.onSuccess {
                    messageProvider.shortPopup(getString(R.string.bookmark_updated_message))
                    finish()
                }
                result.onFailure { exception ->
                    messageProvider.longPopup(getString(R.string.update_bookmark_error_message))
                    Log.e(TAG, UPDATE_BOOKMARK_ERROR_MESSAGE, exception)
                }
            }
        }
    }

    override fun onCaptureClick() {
        photoFile = null
        try {
            photoFile = createUniqueImageFile(this)
        } catch (ex: IOException) {
            return
        }

        photoFile?.let { photoFile ->
            val photoUri = FileProvider.getUriForFile(
                this,
                "hr.from.ivantoplak.placebook.fileprovider",
                photoFile
            )

            val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            }

            val intentActivities = packageManager.queryIntentActivities(
                captureIntent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            intentActivities.map { it.activityInfo.packageName }
                .forEach {
                    grantUriPermission(it, photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }

            startActivityForResult(captureIntent, REQUEST_CAPTURE_IMAGE)
        }
    }

    override fun onPickClick() {
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickIntent, REQUEST_GALLERY_IMAGE)
    }

    private fun replaceImage() {
        val newFragment = PhotoOptionDialogFragment.newInstance(this)
        newFragment?.show(supportFragmentManager, "photoOptionDialog")
    }

    private fun updateImage(image: Bitmap) {
        bookmarkDetailsViewData?.let {
            imageViewPlace.setImageBitmap(image)
            bookmarkDetailsViewModel.setImage(image, it.id)
        }
    }

    private fun getImageWithPath(filePath: String): Bitmap? = decodeFileToSize(
        filePath,
        resources.getDimensionPixelSize(R.dimen.default_image_width),
        resources.getDimensionPixelSize(R.dimen.default_image_height)
    )

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == android.app.Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CAPTURE_IMAGE -> {
                    photoFile?.let { photo ->
                        val uri = FileProvider.getUriForFile(
                            this,
                            "hr.from.ivantoplak.placebook.fileprovider",
                            photo
                        )
                        revokeUriPermission(
                            uri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                        val image = getImageWithPath(photo.absolutePath)
                        image?.let { updateImage(it) }
                    }
                }
                REQUEST_GALLERY_IMAGE -> data?.data?.let { imageUri ->
                    val image = getImageWithAuthority(imageUri)
                    image?.let { updateImage(it) }
                }
            }
        }
    }

    private fun getImageWithAuthority(uri: Uri): Bitmap? = decodeUriStreamToSize(
        uri,
        resources.getDimensionPixelSize(R.dimen.default_image_width),
        resources.getDimensionPixelSize(R.dimen.default_image_height),
        this
    )

    private fun populateCategoryList() {
        bookmarkDetailsViewData?.let { bookmarkView ->
            val resourceId = bookmarkDetailsViewModel.getCategoryResourceId(bookmarkView.category)
            imageViewCategory.setImageResource(resourceId)

            val categories = bookmarkDetailsViewModel.getCategories()
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategory.adapter = adapter

            spinnerCategory.setSelection(adapter.getPosition(bookmarkView.category))

            spinnerCategory.post {
                spinnerCategory.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>,
                            view: View,
                            position: Int,
                            id: Long
                        ) {
                            val category = parent.getItemAtPosition(position) as String
                            val resId = bookmarkDetailsViewModel.getCategoryResourceId(category)
                            imageViewCategory.setImageResource(resId)
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {
// NOTE: This method is required but not used.
                        }
                    }
            }
        }
    }

    private fun deleteBookmark() {
        bookmarkDetailsViewData?.let { bookmarkView ->
            AlertDialog.Builder(this)
                .setMessage("Delete?")
                .setPositiveButton("Ok") { _, _ ->
                    lifecycle.coroutineScope.launch {
                        val result = runCatching {
                            bookmarkDetailsViewModel.deleteBookmark(bookmarkView)
                        }
                        result.onSuccess {
                            messageProvider.shortPopup(getString(R.string.bookmark_deleted_message))
                            finish()
                        }
                        result.onFailure { exception ->
                            messageProvider.longPopup(getString(R.string.delete_bookmark_error_message))
                            Log.e(TAG, DELETE_BOOKMARK_ERROR_MESSAGE, exception)
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .create().show()
        }
    }

    private fun sharePlace() {
        bookmarkDetailsViewData?.let { bookmarkView ->
            val mapUrl = if (bookmarkView.placeId.isEmpty()) {
                val location =
                    URLEncoder.encode("${bookmarkView.latitude},${bookmarkView.longitude}", "utf-8")
                "https://www.google.com/maps/dir/?api=1&destination=$location"
            } else {
                val name = URLEncoder.encode(bookmarkView.name, "utf-8")
                "https://www.google.com/maps/dir/?api=1&destination=$name&destination_place_id=${bookmarkView.placeId}"
            }

            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Check out ${bookmarkView.name} at:\n$mapUrl")
                putExtra(Intent.EXTRA_SUBJECT, "Sharing ${bookmarkView.name}")
                type = "text/plain"
            }
            startActivity(sendIntent)
        }
    }

    private fun setupFab() = fab.setOnClickListener { sharePlace() }
}
