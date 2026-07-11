package com.kingkharnivore.chefesque.data

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kingkharnivore.chefesque.data.local.db.ChefesqueDatabase
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
    ).addMigrations(MIGRATION_1_2).build()

    val recipeRepository = RecipeRepository(database)
    val cookingLogRepository = CookingLogRepository(database)
    val ingredientRepository = IngredientRepository(database)
    val cookSessionRepository = CookSessionRepository(database)

    val ingredientSeeder = IngredientSeeder(
        seedDataSource = IngredientSeedDataSource(context.applicationContext),
        ingredientRepository = ingredientRepository,
    )
}


private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE cook_sessions ADD COLUMN timerOriginalSeconds INTEGER")
        db.execSQL("ALTER TABLE cook_sessions ADD COLUMN timerRemainingSeconds INTEGER")
        db.execSQL("ALTER TABLE cook_sessions ADD COLUMN timerStatus TEXT")
        db.execSQL("ALTER TABLE cook_sessions ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
    }
}
