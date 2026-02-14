package com.timofte.nutrismart.features.nutrition.repository

import com.timofte.nutrismart.features.nutrition.model.ShoppingList
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying

interface ShoppingListRepository : JpaRepository<ShoppingList, Long> {
    fun findByUserId(userId: Long): ShoppingList?

    @Modifying
    @Transactional
    fun deleteByUserId(userId: Long)
}