package com.timofte.nutrismart.features.nutrition.repository

import com.timofte.nutrismart.features.nutrition.model.ShoppingListItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ShoppingListItemRepository : JpaRepository<ShoppingListItem, Long> {

    @Modifying
    @Query("UPDATE ShoppingListItem s SET s.checked = false WHERE s.shoppingList.userId = :userId")
    fun resetAllItemsForUser(@Param("userId") userId: Long)
}