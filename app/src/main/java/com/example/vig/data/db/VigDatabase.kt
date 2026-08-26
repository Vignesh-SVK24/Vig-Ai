package com.example.vig.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UserPreferences::class, Memory::class, TaskEntity::class], version = 1, exportSchema = false)
abstract class VigDatabase : RoomDatabase() {
    // abstract fun memoryDao(): MemoryDao
}
