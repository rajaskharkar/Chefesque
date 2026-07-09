package com.kingkharnivore.chefesque.data.local.seed

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class IngredientSeedDataSource(private val context: Context) {
    fun loadSeedIngredients(): SeedIngredientFile = parse(
        context.assets.open(SEED_FILE_NAME).bufferedReader().use { it.readText() },
    )

    companion object {
        const val SEED_FILE_NAME = "seed_ingredients.json"

        fun parse(json: String): SeedIngredientFile {
            val root = JSONObject(json)
            val ingredientsJson = root.getJSONArray("ingredients")
            return SeedIngredientFile(
                version = root.getInt("version"),
                ingredients = List(ingredientsJson.length()) { index -> ingredientsJson.getJSONObject(index).toDto() },
            )
        }

        fun commonUnitsToJson(commonUnits: List<String>): String = JSONArray(commonUnits).toString()

        private fun JSONObject.toDto(): SeedIngredientDto = SeedIngredientDto(
            id = getString("id"),
            displayName = getString("displayName"),
            canonicalName = getString("canonicalName"),
            category = optStringOrNull("category"),
            defaultUnit = optStringOrNull("defaultUnit"),
            commonUnits = optJSONArray("commonUnits").toStringList(),
            aliases = optJSONArray("aliases").toStringList(),
        )

        private fun JSONObject.optStringOrNull(name: String): String? =
            if (isNull(name)) null else optString(name).takeIf { it.isNotBlank() }

        private fun JSONArray?.toStringList(): List<String> = if (this == null) {
            emptyList()
        } else {
            List(length()) { index -> getString(index) }
        }
    }
}
