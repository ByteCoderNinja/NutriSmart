package com.timofte.nutrismart.features.nutrition.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@Entity
@Table(name = "shopping_items")
data class ShoppingListItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val category: String,
    val name: String,

    var isChecked: Boolean = false,

    @ManyToOne
    @JoinColumn(name = "shopping_list_id")
    @JsonIgnore
    val shoppingList: ShoppingList
)