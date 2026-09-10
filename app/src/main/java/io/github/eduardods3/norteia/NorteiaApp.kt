package io.github.eduardods3.norteia

import android.app.Application
import io.github.eduardods3.norteia.di.AppContainer

/**
 * Application entry point. Holds the [AppContainer] (a tiny hand-rolled service
 * locator) so we avoid pulling in a DI framework for a one-screen app.
 *
 * When ads are added later, MobileAds + the UMP consent flow are initialised
 * here — see docs/MONETIZATION.md.
 */
class NorteiaApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
