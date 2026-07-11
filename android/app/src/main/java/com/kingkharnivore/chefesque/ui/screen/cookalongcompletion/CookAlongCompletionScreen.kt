package com.kingkharnivore.chefesque.ui.screen.cookalongcompletion

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kingkharnivore.chefesque.ui.theme.ChefesqueTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookAlongCompletionScreen(
    uiState: CookAlongCompletionUiState,
    onBackClick: () -> Unit,
    onResultSelected: (String?) -> Unit,
    onWouldMakeAgainSelected: (String?) -> Unit,
    onNotesChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBackClick)
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Cook Complete") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.sessionNotFound -> CompletionMessageState("Cook session not found.", "Back to recipes", onBackClick)
                uiState.sessionNotCompleted -> CompletionMessageState("This cook is not completed yet.", "Back", onBackClick)
                uiState.alreadyLogged -> CompletionMessageState("This cook has already been logged.", "Back to recipe", onSkipClick)
                else -> CookAlongCompletionContent(
                    uiState = uiState,
                    onResultSelected = onResultSelected,
                    onWouldMakeAgainSelected = onWouldMakeAgainSelected,
                    onNotesChange = onNotesChange,
                    onSaveClick = onSaveClick,
                    onSkipClick = onSkipClick,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun CookAlongCompletionContent(
    uiState: CookAlongCompletionUiState,
    onResultSelected: (String?) -> Unit,
    onWouldMakeAgainSelected: (String?) -> Unit,
    onNotesChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = if (uiState.title.isBlank()) "Nice work — cook complete" else "Nice work — you cooked ${uiState.title}",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "Want to remember how it went? Add a quick note now, or skip logging for later.",
            style = MaterialTheme.typography.bodyMedium,
        )
        CompletionSummaryCard(uiState)
        ChoiceSection(
            title = "Quick result",
            choices = CookAlongResultChoices,
            selected = uiState.result,
            labelForChoice = ::resultLabel,
            onSelected = onResultSelected,
        )
        ChoiceSection(
            title = "Would make again?",
            choices = WouldMakeAgainChoices,
            selected = uiState.wouldMakeAgain,
            labelForChoice = ::wouldMakeAgainLabel,
            onSelected = onWouldMakeAgainSelected,
        )
        OutlinedTextField(
            value = uiState.notesForNextTime,
            onValueChange = onNotesChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notes for next time") },
            placeholder = { Text("What would you change or remember?") },
            minLines = 3,
        )
        uiState.saveError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = onSaveClick,
            enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (uiState.isSaving) "Saving…" else "Save Cooking Log") }
        TextButton(onClick = onSkipClick, modifier = Modifier.fillMaxWidth()) { Text("Skip for now") }
    }
}

@Composable
private fun CompletionSummaryCard(uiState: CookAlongCompletionUiState) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Summary", style = MaterialTheme.typography.titleMedium)
            SummaryLine("Recipe", uiState.title.ifBlank { "Cook Along" })
            uiState.completedAtText?.let { SummaryLine("Completed", it) }
            uiState.durationText?.let { SummaryLine("Duration", it) }
            SummaryLine("Steps", uiState.completedStepsText)
            SummaryLine("Source", "Completed from Cook Along")
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChoiceSection(
    title: String,
    choices: List<String>,
    selected: String?,
    labelForChoice: (String?) -> String?,
    onSelected: (String?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            choices.forEach { choice ->
                FilterChip(
                    selected = selected == choice,
                    onClick = { onSelected(if (selected == choice) null else choice) },
                    label = { Text(labelForChoice(choice).orEmpty()) },
                )
            }
        }
    }
}

@Composable
private fun CompletionMessageState(message: String, actionLabel: String, onActionClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(message, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onActionClick) { Text(actionLabel) }
    }
}

@Preview(showBackground = true)
@Composable
private fun CookAlongCompletionPreview() {
    ChefesqueTheme {
        CookAlongCompletionScreen(
            uiState = CookAlongCompletionUiState(
                isLoading = false,
                title = "Weeknight Pasta",
                completedAtText = "Jul 11, 2026",
                durationText = "42 min",
                completedStepsText = "Completed 6 steps",
            ),
            onBackClick = {},
            onResultSelected = {},
            onWouldMakeAgainSelected = {},
            onNotesChange = {},
            onSaveClick = {},
            onSkipClick = {},
        )
    }
}
