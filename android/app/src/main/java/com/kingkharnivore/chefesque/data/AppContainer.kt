package com.kingkharnivore.chefesque.data

import android.content.Context
import androidx.room.Room
import com.kingkharnivore.chefesque.data.local.db.ChefesqueDatabase
import com.kingkharnivore.chefesque.data.local.db.MIGRATION_1_2
import com.kingkharnivore.chefesque.data.local.db.MIGRATION_2_3
import com.kingkharnivore.chefesque.data.local.seed.IngredientSeedDataSource
import com.kingkharnivore.chefesque.data.local.seed.IngredientSeeder
import com.kingkharnivore.chefesque.data.repository.CookSessionRepository
import com.kingkharnivore.chefesque.data.repository.CookingLogRepository
import com.kingkharnivore.chefesque.data.repository.IngredientRepository
import com.kingkharnivore.chefesque.data.repository.RecipeRepository

class AppContainer(context: Context) {
    val database: ChefesqueDatabase = Room.databaseBuilder(
        context.applicationContext,
        ChefesqueDatabase::class.java,
        "chefesque.db",
    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()

    val recipeRepository = RecipeRepository(database)
    val cookingLogRepository = CookingLogRepository(database)
    val ingredientRepository = IngredientRepository(database)
    val cookSessionRepository = CookSessionRepository(database)

    val ingredientSeeder = IngredientSeeder(
        seedDataSource = IngredientSeedDataSource(context.applicationContext),
        ingredientRepository = ingredientRepository,
    )
}
