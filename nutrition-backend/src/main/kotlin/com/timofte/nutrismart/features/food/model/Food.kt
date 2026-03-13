package com.timofte.nutrismart.features.food.model

import com.timofte.nutrismart.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "foods")
class Food(
    @Column(nullable = false, unique = true)
    var name: String = ""
) : BaseEntity()
