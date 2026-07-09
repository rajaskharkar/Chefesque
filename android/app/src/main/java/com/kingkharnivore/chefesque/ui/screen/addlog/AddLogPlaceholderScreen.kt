package com.kingkharnivore.chefesque.ui.screen.addlog

import androidx.compose.runtime.Composable
import com.kingkharnivore.chefesque.ui.screen.main.PlaceholderScreen

@Composable
fun AddLogPlaceholderScreen(onBackClick: () -> Unit) {
    PlaceholderScreen(title = "Add Log", body = "Cooking log creation starts in a later pass.", onBackClick = onBackClick)
}
