package hr.from.ivantoplak.placebook.di

import androidx.room.Room
import hr.from.ivantoplak.placebook.adapter.BookmarkInfoWindowAdapter
import hr.from.ivantoplak.placebook.coroutines.CoroutineContextProvider
import hr.from.ivantoplak.placebook.coroutines.CoroutineContextProviderImpl
import hr.from.ivantoplak.placebook.db.PlaceBookDatabase
import hr.from.ivantoplak.placebook.repository.BookmarkRepo
import hr.from.ivantoplak.placebook.repository.BookmarkRepoImpl
import hr.from.ivantoplak.placebook.ui.MapsActivity
import hr.from.ivantoplak.placebook.util.image.BitmapImageProvider
import hr.from.ivantoplak.placebook.util.image.BookmarkImageProviderImpl
import hr.from.ivantoplak.placebook.util.ui.MessageProvider
import hr.from.ivantoplak.placebook.util.ui.MessageProviderImpl
import hr.from.ivantoplak.placebook.viewmodel.BookmarkDetailsViewModel
import hr.from.ivantoplak.placebook.viewmodel.MapsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    //Room database
    single {
        Room.databaseBuilder(
            androidContext(),
            PlaceBookDatabase::class.java,
            PlaceBookDatabase.NAME
        ).build()
    }

    single { get<PlaceBookDatabase>().bookmarkDao() }

    single<CoroutineContextProvider> { CoroutineContextProviderImpl() }

    single<BitmapImageProvider> { BookmarkImageProviderImpl(androidContext()) }

    single<MessageProvider> { MessageProviderImpl(androidContext()) }

    single<BookmarkRepo> { BookmarkRepoImpl(get()) }

    viewModel { MapsViewModel(get(), get(), get()) }

    viewModel { BookmarkDetailsViewModel(get(), get(), get()) }

    scope<MapsActivity> {
        scoped { BookmarkInfoWindowAdapter(get(), get()) }
    }
}