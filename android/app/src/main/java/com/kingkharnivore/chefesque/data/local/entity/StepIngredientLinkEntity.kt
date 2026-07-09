package com.kingkharnivore.chefesque.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "step_ingredient_links",
    primaryKeys = ["stepId", "recipeIngredientId"],
    foreignKeys = [
        ForeignKey(entity = RecipeStepEntity::class, parentColumns = ["id"], childColumns = ["stepId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = RecipeIngredientEntity::class, parentColumns = ["id"], childColumns = ["recipeIngredientId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("stepId"), Index("recipeIngredientId")],
)
data class StepIngredientLinkEntity(
    val stepId: String,
    val recipeIngredientId: String,
)
