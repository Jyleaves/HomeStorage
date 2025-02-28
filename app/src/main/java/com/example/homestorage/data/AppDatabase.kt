package com.example.homestorage.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.homestorage.util.StringListConverter

@Database(
    entities = [Item::class, RoomEntity::class, ContainerEntity::class, ItemCategory::class, SubContainerEntity::class, ThirdContainerEntity::class],
    version = 18,
    exportSchema = true
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun roomDao(): RoomDao
    abstract fun containerDao(): ContainerDao
    abstract fun subContainerDao(): SubContainerDao
    abstract fun thirdContainerDao(): ThirdContainerDao
    abstract fun itemCategoryDao(): ItemCategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "home_inventory_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
