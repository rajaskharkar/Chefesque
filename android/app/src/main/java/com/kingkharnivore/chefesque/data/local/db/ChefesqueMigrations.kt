package com.kingkharnivore.chefesque.data.local.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Timer fields are Cook Along snapshots only; running timers restore as paused until background timers are intentionally implemented.
        db.execSQL("ALTER TABLE cook_sessions ADD COLUMN timerOriginalSeconds INTEGER")
        db.execSQL("ALTER TABLE cook_sessions ADD COLUMN timerRemainingSeconds INTEGER")
        db.execSQL("ALTER TABLE cook_sessions ADD COLUMN timerStatus TEXT")
        db.execSQL("ALTER TABLE cook_sessions ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
    }
}


val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE recipes ADD COLUMN lifecycleStatus TEXT NOT NULL DEFAULT 'PUBLISHED'")
        db.execSQL("ALTER TABLE recipes ADD COLUMN lastEditedAt INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE recipes ADD COLUMN publishedAt INTEGER")
        db.execSQL("ALTER TABLE recipes ADD COLUMN lastEditedTab TEXT NOT NULL DEFAULT 'BASIC_INFO'")
        db.execSQL("ALTER TABLE recipes ADD COLUMN sourceRecipeId TEXT")
        db.execSQL("ALTER TABLE recipes ADD COLUMN hasUnpublishedChanges INTEGER NOT NULL DEFAULT 0")
        db.execSQL("UPDATE recipes SET lastEditedAt = updatedAt, publishedAt = updatedAt WHERE lastEditedAt = 0")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_recipes_lifecycleStatus ON recipes(lifecycleStatus)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_recipes_lastEditedAt ON recipes(lastEditedAt)")
        db.execSQL("ALTER TABLE recipe_steps ADD COLUMN title TEXT")
        db.execSQL("ALTER TABLE recipe_steps ADD COLUMN meanwhile TEXT")
        db.execSQL("UPDATE recipe_steps SET meanwhile = whileTimerRuns WHERE whileTimerRuns IS NOT NULL")
    }
}
