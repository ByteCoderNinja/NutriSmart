package com.timofte.nutrismart.features.nutrition.repository

import com.timofte.nutrismart.features.nutrition.model.ShoppingListItem
import org.springframework.data.jpa.repository.JpaRepository

interface ShoppingListItemRepository : JpaRepository<ShoppingListItem, Long>