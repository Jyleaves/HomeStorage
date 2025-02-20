package com.example.homestorage.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.homestorage.data.AppDatabase
import com.example.homestorage.data.ItemCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ItemCategoryViewModel(application: Application) : AndroidViewModel(application) {
    private val itemCategoryDao = AppDatabase.getDatabase(application).itemCategoryDao()

    val allCategories: Flow<List<ItemCategory>> = itemCategoryDao.getAllCategories()

    fun insert(category: ItemCategory) {
        viewModelScope.launch {
            itemCategoryDao.insert(category)
        }
    }

    fun delete(category: ItemCategory) {
        viewModelScope.launch {
            // 先删除该类别的所有物品
            AppDatabase.getDatabase(getApplication()).itemDao().deleteItemsByCategory(category.categoryName)
            // 再删除类别本身
            itemCategoryDao.delete(category)
        }
    }

    fun updateCategory(oldCategory: ItemCategory, newCategory: ItemCategory) {
        viewModelScope.launch {
            // 1. 更新类别记录本身
            itemCategoryDao.update(newCategory)

            // 2. 获取该类别下所有物品
            val itemDao = AppDatabase.getDatabase(getApplication()).itemDao()
            val items = itemDao.getItemsByCategory(oldCategory.categoryName)

            // 3. 根据新旧属性差异，批量更新物品
            val updatedItems = items.map { item ->
                var newItem = item

                // 如果类别名称允许改，要把 item.category 替换为 newCategory.categoryName
                if (oldCategory.categoryName != newCategory.categoryName) {
                    newItem = newItem.copy(category = newCategory.categoryName)
                }

                // 如果 needExpirationDate 从 true => false，则清空物品的 expirationDate
                if (oldCategory.needExpirationDate && !newCategory.needExpirationDate) {
                    newItem = newItem.copy(expirationDate = null)
                }

                // 如果 needReminder 从 true => false，则清空物品的 reminderDays
                if (oldCategory.needReminder && !newCategory.needReminder) {
                    newItem = newItem.copy(reminderDays = null)
                }

                // 如果 needProductionDate 从 true => false，则清空物品的 productionDate
                if (oldCategory.needProductionDate && !newCategory.needProductionDate) {
                    newItem = newItem.copy(productionDate = null)
                }

                // 如果 needQuantity 从 true => false，则清空物品的 quantity
                if (oldCategory.needQuantity && !newCategory.needQuantity) {
                    newItem = newItem.copy(quantity = null)
                }

                if (!oldCategory.needReminder && newCategory.needReminder) {
                    // 假设让物品的 reminderDays = newCategory.reminderPeriodDays
                    newItem = newItem.copy(reminderDays = newCategory.reminderPeriodDays)
                }

                newItem
            }

            // 4. 批量更新物品
            itemDao.updateItems(updatedItems)
        }
    }
}

