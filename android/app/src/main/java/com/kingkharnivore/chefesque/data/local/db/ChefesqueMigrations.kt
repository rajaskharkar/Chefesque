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
