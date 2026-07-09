package com.kingkharnivore.chefesque.ui.screen.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kingkharnivore.chefesque.data.local.seed.IngredientSeeder
import kotlinx.coroutines.launch

class MainViewModel(
    private val ingredientSeeder: IngredientSeeder,
) : ViewModel() {
    init {
        viewModelScope.launch {
            runCatching { ingredientSeeder.seedIfNeeded() }
                .onFailure { Log.w("Chefesque", "Ingredient seeding failed", it) }
        }
    }
}

class MainViewModelFactory(
    private val ingredientSeeder: IngredientSeeder,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(ingredientSeeder) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
