package com.timofte.nutrismart.features.nutrition.repository

import com.timofte.nutrismart.features.nutrition.model.ShoppingList
import org.springframework.data.jpa.repository.JpaRepository

interface ShoppingListRepository : JpaRepository<ShoppingList, Long> {
    fun findByUserId(userId: Long): ShoppingList?
    fun deleteByUserId(userId: Long)
}