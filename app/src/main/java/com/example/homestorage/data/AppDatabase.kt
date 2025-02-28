package com.example.homestorage.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
                    .addMigrations(MIGRATION_17_18)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // 定义迁移策略[4,5](@ref)
        private val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 创建临时表存储新格式
                db.execSQL("""
                    CREATE TABLE items_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        room TEXT NOT NULL,
                        container TEXT NOT NULL,
                        subContainer TEXT,
                        thirdContainer TEXT,
                        category TEXT NOT NULL,
                        description TEXT NOT NULL,
                        photoUris TEXT NOT NULL,
                        productionDate INTEGER,
                        reminderDays INTEGER,
                        quantity INTEGER,
                        timestamp INTEGER NOT NULL,
                        expirationDate INTEGER
                    )
                """.trimIndent())

                // 迁移旧数据：将单图URI转换为数组格式[6](@ref)
                db.execSQL("""
                    INSERT INTO items_new 
                    SELECT id,name,room,container,subContainer,thirdContainer,category,
                           description,
                           json_array(photoUri), -- 将旧字段转为JSON数组
                           productionDate,reminderDays,quantity,timestamp,expirationDate
                    FROM items
                """.trimIndent())

                // 删除旧表并重命名
                db.execSQL("DROP TABLE items")
                db.execSQL("ALTER TABLE items_new RENAME TO items")
            }
        }
    }
}
