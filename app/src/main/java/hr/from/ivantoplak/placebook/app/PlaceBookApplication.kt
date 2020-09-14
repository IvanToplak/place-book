package hr.from.ivantoplak.placebook.app

import android.app.Application
import hr.from.ivantoplak.placebook.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class PlaceBookApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@PlaceBookApplication)
            androidLogger(Level.ERROR)
            modules(listOf(appModule))
        }
    }
}