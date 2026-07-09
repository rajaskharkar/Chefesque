package com.kingkharnivore.chefesque.ui.screen.addrecipe

import androidx.compose.runtime.Composable
import com.kingkharnivore.chefesque.ui.screen.main.PlaceholderScreen

@Composable
fun AddRecipePlaceholderScreen(onBackClick: () -> Unit) {
    PlaceholderScreen(title = "Add Recipe", body = "Recipe creation starts in the next pass.", onBackClick = onBackClick)
}
