package com.kingkharnivore.chefesque.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kingkharnivore.chefesque.data.local.dao.CookSessionDao
import com.kingkharnivore.chefesque.data.local.dao.CookingLogDao
import com.kingkharnivore.chefesque.data.local.dao.CookingLogPhotoDao
import com.kingkharnivore.chefesque.data.local.dao.IngredientDao
import com.kingkharnivore.chefesque.data.local.dao.RecipeComponentDao
import com.kingkharnivore.chefesque.data.local.dao.RecipeDao
import com.kingkharnivore.chefesque.data.local.dao.RecipeIngredientDao
import com.kingkharnivore.chefesque.data.local.dao.RecipeStepDao
import com.kingkharnivore.chefesque.data.local.entity.CookSessionComponentStatusEntity
import com.kingkharnivore.chefesque.data.local.entity.CookSessionEntity
import com.kingkharnivore.chefesque.data.local.entity.CookingLogEntity
import com.kingkharnivore.chefesque.data.local.entity.CookingLogPhotoEntity
import com.kingkharnivore.chefesque.data.local.entity.IngredientAliasEntity
import com.kingkharnivore.chefesque.data.local.entity.IngredientEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeComponentEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeIngredientEntity
import com.kingkharnivore.chefesque.data.local.entity.RecipeStepEntity
import com.kingkharnivore.chefesque.data.local.entity.StepIngredientLinkEntity

@Database(
    entities = [
        RecipeEntity::class,
        IngredientEntity::class,
        IngredientAliasEntity::class,
        RecipeIngredientEntity::class,
        RecipeComponentEntity::class,
        RecipeStepEntity::class,
        StepIngredientLinkEntity::class,
        CookSessionEntity::class,
        CookSessionComponentStatusEntity::class,
        CookingLogEntity::class,
        CookingLogPhotoEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class ChefesqueDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeIngredientDao(): RecipeIngredientDao
    abstract fun recipeComponentDao(): RecipeComponentDao
    abstract fun recipeStepDao(): RecipeStepDao
    abstract fun cookSessionDao(): CookSessionDao
    abstract fun cookingLogDao(): CookingLogDao
    abstract fun cookingLogPhotoDao(): CookingLogPhotoDao
}
