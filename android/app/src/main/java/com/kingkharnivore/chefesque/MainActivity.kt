package com.kingkharnivore.chefesque

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.kingkharnivore.chefesque.ui.navigation.ChefesqueApp
import com.kingkharnivore.chefesque.ui.screen.main.MainViewModel
import com.kingkharnivore.chefesque.ui.screen.main.MainViewModelFactory
import com.kingkharnivore.chefesque.ui.theme.ChefesqueTheme

class MainActivity : ComponentActivity() {
    private val appContainer by lazy { (application as ChefesqueApplication).appContainer }
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(appContainer.ingredientSeeder)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mainViewModel
        setContent {
            ChefesqueTheme {
                ChefesqueApp(appContainer = appContainer)
            }
        }
    }
}
