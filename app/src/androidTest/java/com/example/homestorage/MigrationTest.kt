package com.example.homestorage

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.homestorage.data.AppDatabase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val TEST_DB = "migration-test"

    // 添加Room测试规则
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        listOf(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate17To18() {
        // 创建v17数据库
        helper.createDatabase(TEST_DB, 17).apply {
            // 插入测试数据（旧格式）
            execSQL("""
                INSERT INTO items(
                    name, room, container, category, description, photoUri, timestamp
                ) VALUES(
                    'testItem', '客厅', '柜子', '工具', '测试物品', 'content://old_photo', 123456
                )
            """.trimIndent())
            close()
        }

        // 执行迁移到v18
        val db = helper.runMigrationsAndValidate(TEST_DB, 18, true)

        // 验证迁移结果
        db.query("SELECT photoUris FROM items").use { cursor ->
            assertTrue("Cursor should have data", cursor.moveToFirst())
            assertEquals(
                "JSON数组格式检查",
                "[\"content://old_photo\"]",
                cursor.getString(0)
            )
        }
    }
}