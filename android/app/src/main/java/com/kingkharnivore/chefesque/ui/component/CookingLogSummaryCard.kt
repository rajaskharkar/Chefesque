package com.kingkharnivore.chefesque.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kingkharnivore.chefesque.ui.screen.cookinglog.CookingLogCardUiModel
import com.kingkharnivore.chefesque.ui.theme.ChefesqueTheme

@Composable
fun CookingLogSummaryCard(log: CookingLogCardUiModel, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(log.title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (log.isFavorite) Text("★", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            Text(
                listOfNotNull(log.cookedDateText, log.durationText).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                log.resultText?.let { SummaryChip(it) }
                log.wouldMakeAgainText?.let { SummaryChip(it) }
                if (log.createdFromCookAlong) SummaryChip("Cook Along")
            }
            log.notesPreview?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SummaryChip(label: String) {
    AssistChip(onClick = {}, label = { Text(label) }, enabled = false)
}

@Preview(showBackground = true)
@Composable
private fun CookingLogSummaryCardPreview() {
    ChefesqueTheme {
        CookingLogSummaryCard(
            log = CookingLogCardUiModel(
                id = "1",
                title = "Weeknight Pasta",
                cookedDateText = "Jul 11, 2026",
                durationText = "45 min",
                resultText = "Good",
                wouldMakeAgainText = "Would make again",
                notesPreview = "Use less salt next time.",
                isFavorite = true,
                createdFromCookAlong = true,
            ),
            onClick = {},
        )
    }
}
