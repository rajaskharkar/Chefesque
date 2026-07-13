package com.kingkharnivore.chefesque.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kingkharnivore.chefesque.data.local.entity.RecipeEntity
import com.kingkharnivore.chefesque.ui.theme.ChefesqueTheme

@Composable
fun RecipeSummaryCard(recipe: RecipeEntity, onClick: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    val metadata = buildList {
        recipe.servings?.takeIf { it > 0 }?.let { add("Serves $it") }
        recipe.prepTimeMinutes?.takeIf { it > 0 }?.let { add("Prep $it min") }
        recipe.cookTimeMinutes?.takeIf { it > 0 }?.let { add("Cook $it min") }
    }.joinToString(" · ")
    val cardModifier = if (onClick != null) modifier.fillMaxWidth().clickable(onClick = onClick) else modifier.fillMaxWidth()
    OutlinedCard(
        modifier = cardModifier,
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(recipe.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (metadata.isNotBlank()) Text(metadata, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            recipe.recipeType?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary) }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RecipeSummaryCardPreview() {
    ChefesqueTheme {
        RecipeSummaryCard(RecipeEntity("1", "Sunday Sauce", null, 6, 20, 180, null, null, null, "Full dish", null, 0, 0, null))
    }
}
