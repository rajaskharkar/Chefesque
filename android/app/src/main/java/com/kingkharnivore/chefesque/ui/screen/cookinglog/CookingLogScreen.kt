package com.kingkharnivore.chefesque.ui.screen.cookinglog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kingkharnivore.chefesque.ui.component.ChefesqueEmptyState
import com.kingkharnivore.chefesque.ui.component.CookingLogSummaryCard
import com.kingkharnivore.chefesque.ui.theme.ChefesqueTheme

@Composable
fun CookingLogScreen(uiState: CookingLogUiState, onAddLogClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            uiState.logs.isEmpty() -> ChefesqueEmptyState(
                title = "No cooking logs yet",
                body = "Finish a Cook Along to save your first cooking log.",
                actionLabel = null,
                onActionClick = onAddLogClick,
                modifier = Modifier.align(Alignment.Center),
            )
            else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(uiState.logs, key = { it.id }) { CookingLogSummaryCard(it) }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CookingLogScreenEmptyPreview() {
    ChefesqueTheme { CookingLogScreen(CookingLogUiState(isLoading = false), {}) }
}
