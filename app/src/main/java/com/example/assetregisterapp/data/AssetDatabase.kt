package com.example.assetregisterapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Asset::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AssetDatabase : RoomDatabase() {
    abstract fun assetDao(): AssetDao

    companion object {
        @Volatile
        private var INSTANCE: AssetDatabase? = null

        fun getDatabase(context: Context): AssetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AssetDatabase::class.java,
                    "asset_database"
                )
                .fallbackToDestructiveMigration() // This will recreate DB on schema changes
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}