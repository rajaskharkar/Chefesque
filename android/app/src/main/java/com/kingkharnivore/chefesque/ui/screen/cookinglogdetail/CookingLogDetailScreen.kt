package com.kingkharnivore.chefesque.ui.screen.cookinglogdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kingkharnivore.chefesque.ui.theme.ChefesqueTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingLogDetailScreen(
    uiState: CookingLogDetailUiState,
    onBackClick: () -> Unit,
    onViewRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Cooking Log") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.notFound -> CookingLogNotFound(onBackClick, Modifier.align(Alignment.Center).padding(24.dp))
                else -> CookingLogDetailContent(uiState, onViewRecipeClick)
            }
        }
    }
}

@Composable
private fun CookingLogDetailContent(uiState: CookingLogDetailUiState, onViewRecipeClick: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HeaderSection(uiState)
        SummarySection(uiState)
        NotesSection(uiState)
        LinkedRecipeSection(uiState, onViewRecipeClick)
    }
}

@Composable
private fun HeaderSection(uiState: CookingLogDetailUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(uiState.title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            if (uiState.isFavorite) Text("★", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        }
        Text(uiState.cookedDateText, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SummarySection(uiState: CookingLogDetailUiState) {
    val rows = listOfNotNull(
        uiState.durationText?.let { "Duration" to it },
        uiState.resultText?.let { "Result" to it },
        uiState.wouldMakeAgainText?.let { "Would make again" to it },
        uiState.sourceText?.let { "Source" to it },
    )
    if (rows.isEmpty()) return
    DetailSectionCard("Summary") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            rows.forEach { (label, value) -> SummaryRow(label, value) }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun NotesSection(uiState: CookingLogDetailUiState) {
    val notes = listOfNotNull(
        uiState.notesForNextTime?.let { "Notes for next time" to it },
        uiState.changesMade?.let { "Changes made" to it },
        uiState.whatWentWell?.let { "What went well" to it },
    )
    DetailSectionCard("Notes") {
        if (notes.isEmpty()) {
            Text("No notes saved for this cook.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                notes.forEach { (label, value) ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(value, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun LinkedRecipeSection(uiState: CookingLogDetailUiState, onViewRecipeClick: (String) -> Unit) {
    val recipeId = uiState.recipeId
    DetailSectionCard("Recipe") {
        when {
            recipeId == null -> Text("No linked recipe", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            uiState.recipeAvailable -> {
                Text("This cook is linked to a saved recipe.", style = MaterialTheme.typography.bodyMedium)
                Button(onClick = { onViewRecipeClick(recipeId) }, modifier = Modifier.fillMaxWidth()) { Text("View Recipe") }
            }
            else -> Text("Recipe unavailable", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DetailSectionCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun CookingLogNotFound(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Cooking log not found.", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Button(onClick = onBackClick) { Text("Back") }
    }
}

@Preview(showBackground = true)
@Composable
private fun CookingLogDetailScreenPreview() {
    ChefesqueTheme {
        CookingLogDetailScreen(
            uiState = CookingLogDetailUiState(
                isLoading = false,
                logId = "log1",
                recipeId = "recipe1",
                recipeAvailable = true,
                title = "Sunday Sauce",
                cookedDateText = "Jul 11, 2026",
                durationText = "1 hr 30 min",
                resultText = "Great",
                wouldMakeAgainText = "Would make again",
                sourceText = "Created from Cook Along",
                notesForNextTime = "Use a little less salt.",
                changesMade = "Added extra basil.",
                whatWentWell = "The sauce reduced beautifully.",
                isFavorite = true,
            ),
            onBackClick = {},
            onViewRecipeClick = {},
        )
    }
}
