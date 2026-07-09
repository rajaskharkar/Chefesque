package com.kingkharnivore.chefesque

import android.app.Application
import com.kingkharnivore.chefesque.data.AppContainer

class ChefesqueApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
