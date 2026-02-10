package com.timofte.nutrismart.features.nutrition.model

import jakarta.persistence.*

@Entity
@Table(name = "shopping_lists")
data class ShoppingList(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val userId: Long,

    @OneToMany(mappedBy = "shoppingList", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val items: MutableList<ShoppingListItem> = mutableListOf()
)