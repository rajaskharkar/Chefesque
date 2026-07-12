package com.kingkharnivore.chefesque.ui.screen.cookalong

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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kingkharnivore.chefesque.data.local.entity.RecipeEntity
import com.kingkharnivore.chefesque.ui.theme.ChefesqueTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookAlongScreen(
    uiState: CookAlongUiState,
    onBackClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onFinishClick: () -> Unit,
    onStartTimerClick: () -> Unit,
    onPauseTimerClick: () -> Unit,
    onResumeTimerClick: () -> Unit,
    onResetTimerClick: () -> Unit,
    onAddMinuteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBackClick)
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Cook Along") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        bottomBar = {
            if (!uiState.isLoading && !uiState.notFound && uiState.hasSteps) {
                CookAlongBottomBar(uiState, onPreviousClick, onNextClick, onFinishClick)
            }
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                uiState.isLoading -> CookAlongLoadingState()
                uiState.notFound -> CookAlongMessageState("Recipe not found", "This recipe may have been deleted or archived.", "Back", onBackClick)
                !uiState.hasSteps -> CookAlongMessageState("No steps yet", "This recipe does not have step-by-step instructions yet.", "Back to recipe", onBackClick)
                else -> CookAlongContent(
                    uiState = uiState,
                    onStartTimerClick = onStartTimerClick,
                    onPauseTimerClick = onPauseTimerClick,
                    onResumeTimerClick = onResumeTimerClick,
                    onResetTimerClick = onResetTimerClick,
                    onAddMinuteClick = onAddMinuteClick,
                )
            }
        }
    }
}

@Composable
private fun CookAlongContent(
    uiState: CookAlongUiState,
    onStartTimerClick: () -> Unit,
    onPauseTimerClick: () -> Unit,
    onResumeTimerClick: () -> Unit,
    onResetTimerClick: () -> Unit,
    onAddMinuteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val step = uiState.currentStep ?: return
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(uiState.recipe?.title?.trim()?.ifBlank { "Untitled recipe" } ?: "Untitled recipe", style = MaterialTheme.typography.titleLarge)
        AssistChip(onClick = {}, label = { Text("Step ${uiState.currentStepIndex + 1} of ${uiState.steps.size}") })
        if (uiState.resumedSession) {
            Text("Resumed from your last cook.", style = MaterialTheme.typography.bodyMedium)
        }
        Text(step.instruction, style = MaterialTheme.typography.headlineSmall)
        CookAlongIngredientList(step.ingredients)
        if (uiState.timerOriginalSeconds != null && uiState.timerRemainingSeconds != null) {
            CookAlongTimerCard(
                remainingSeconds = uiState.timerRemainingSeconds,
                status = uiState.timerStatus,
                onStartClick = onStartTimerClick,
                onPauseClick = onPauseTimerClick,
                onResumeClick = onResumeTimerClick,
                onResetClick = onResetTimerClick,
                onAddMinuteClick = onAddMinuteClick,
            )
        }
        step.warning?.let { CookAlongDetailSection("Warning", it) }
        step.equipment?.let { CookAlongDetailSection("Equipment", it) }
        step.whileTimerRuns?.let { CookAlongDetailSection("While timer runs", it) }
        step.checkpoint?.let { CookAlongDetailSection("Checkpoint", it) }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun CookAlongIngredientList(ingredients: List<CookAlongIngredientUiModel>, modifier: Modifier = Modifier) {
    CookAlongDetailSection(title = "Ingredients for this step", modifier = modifier) {
        if (ingredients.isEmpty()) {
            Text("No ingredients linked to this step.", style = MaterialTheme.typography.bodyMedium)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ingredients.forEach { ingredient ->
                    val optional = if (ingredient.optional) " (Optional)" else ""
                    Text("• ${ingredient.displayText}$optional", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun CookAlongTimerCard(
    remainingSeconds: Int,
    status: CookAlongTimerStatus,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onResetClick: () -> Unit,
    onAddMinuteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CookAlongDetailSection(title = "Timer", modifier = modifier) {
        Text(
            text = formatCountdownTime(remainingSeconds),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.secondary,
        )
        Text(timerStatusLabel(status), style = MaterialTheme.typography.bodyMedium)
        if (status == CookAlongTimerStatus.FINISHED) {
            Text("Timer done.", style = MaterialTheme.typography.bodyMedium)
        }
        TimerControlButtons(
            status = status,
            onStartClick = onStartClick,
            onPauseClick = onPauseClick,
            onResumeClick = onResumeClick,
            onResetClick = onResetClick,
            onAddMinuteClick = onAddMinuteClick,
        )
    }
}

@Composable
private fun TimerControlButtons(
    status: CookAlongTimerStatus,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onResetClick: () -> Unit,
    onAddMinuteClick: () -> Unit,
) {
    when (status) {
        CookAlongTimerStatus.IDLE -> Button(onClick = onStartClick, modifier = Modifier.fillMaxWidth(), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary)) { Text("Start") }
        CookAlongTimerStatus.RUNNING -> TimerSecondaryControls(primaryText = "Pause", onPrimaryClick = onPauseClick, onResetClick = onResetClick, onAddMinuteClick = onAddMinuteClick)
        CookAlongTimerStatus.PAUSED -> TimerSecondaryControls(primaryText = "Resume", onPrimaryClick = onResumeClick, onResetClick = onResetClick, onAddMinuteClick = onAddMinuteClick)
        CookAlongTimerStatus.FINISHED -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onResetClick, modifier = Modifier.weight(1f)) { Text("Reset") }
            Button(onClick = onAddMinuteClick, modifier = Modifier.weight(1f)) { Text("+1 min") }
        }
    }
}

@Composable
private fun TimerSecondaryControls(
    primaryText: String,
    onPrimaryClick: () -> Unit,
    onResetClick: () -> Unit,
    onAddMinuteClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onPrimaryClick, modifier = Modifier.fillMaxWidth(), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary)) { Text(primaryText) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onResetClick, modifier = Modifier.weight(1f)) { Text("Reset") }
            OutlinedButton(onClick = onAddMinuteClick, modifier = Modifier.weight(1f)) { Text("+1 min") }
        }
    }
}

private fun timerStatusLabel(status: CookAlongTimerStatus): String = when (status) {
    CookAlongTimerStatus.IDLE -> "Ready"
    CookAlongTimerStatus.RUNNING -> "Running"
    CookAlongTimerStatus.PAUSED -> "Paused"
    CookAlongTimerStatus.FINISHED -> "Done"
}

@Composable
private fun CookAlongDetailSection(title: String, body: String, modifier: Modifier = Modifier) {
    CookAlongDetailSection(title = title, modifier = modifier) {
        Text(body, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun CookAlongDetailSection(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun CookAlongBottomBar(uiState: CookAlongUiState, onPreviousClick: () -> Unit, onNextClick: () -> Unit, onFinishClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = onPreviousClick, enabled = !uiState.isFirstStep, modifier = Modifier.weight(1f)) { Text("Previous") }
        Button(onClick = if (uiState.isLastStep) onFinishClick else onNextClick, modifier = Modifier.weight(1f)) {
            Text(if (uiState.isLastStep) "Finish" else "Next")
        }
    }
}

@Composable
private fun CookAlongLoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
private fun CookAlongMessageState(title: String, body: String, buttonText: String, onBackClick: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(body, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onBackClick) { Text(buttonText) }
    }
}

@Preview(showBackground = true)
@Composable
private fun CookAlongScreenPreview() {
    ChefesqueTheme {
        CookAlongScreen(
            uiState = CookAlongUiState(
                isLoading = false,
                recipe = RecipeEntity("1", "Sunday Sauce", null, 4, null, null, null, null, null, null, null, 0, 0, null),
                steps = listOf(CookAlongStepUiModel("s1", "Add onion and cook until softened.", 480, "Do not let garlic burn.", "Large skillet", "Start pasta water.", "Pause and check before moving on.", listOf(CookAlongIngredientUiModel("i1", "1 onion, diced", false)))),
            ),
            onBackClick = {}, onPreviousClick = {}, onNextClick = {}, onFinishClick = {},
            onStartTimerClick = {}, onPauseTimerClick = {}, onResumeTimerClick = {}, onResetTimerClick = {}, onAddMinuteClick = {},
        )
    }
}
