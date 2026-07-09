package com.kingkharnivore.chefesque.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kingkharnivore.chefesque.data.local.entity.CookingLogEntity
import com.kingkharnivore.chefesque.ui.theme.ChefesqueTheme
import java.text.DateFormat
import java.util.Date

@Composable
fun CookingLogSummaryCard(log: CookingLogEntity, modifier: Modifier = Modifier) {
    val metadata = buildList {
        add(formatCookedAt(log.cookedAt))
        log.actualDurationSeconds?.takeIf { it > 0 }?.let { add(formatDuration(it)) }
        log.result?.takeIf { it.isNotBlank() }?.let { add(it) }
    }.joinToString(" · ")
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(log.titleSnapshot, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(metadata, style = MaterialTheme.typography.bodyMedium)
            log.notesForNextTime?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

private fun formatCookedAt(cookedAt: Long): String = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(cookedAt))
private fun formatDuration(seconds: Int): String = when {
    seconds < 60 -> "${seconds}s"
    seconds % 3600 == 0 -> "${seconds / 3600} hr"
    seconds >= 3600 -> "${seconds / 3600} hr ${(seconds % 3600) / 60} min"
    else -> "${seconds / 60} min"
}

@Preview(showBackground = true)
@Composable
private fun CookingLogSummaryCardPreview() {
    ChefesqueTheme {
        CookingLogSummaryCard(CookingLogEntity("1", null, null, "Weeknight Pasta", System.currentTimeMillis(), 2700, "Good", null, null, null, "Use less salt next time.", false, false, 0, 0))
    }
}
