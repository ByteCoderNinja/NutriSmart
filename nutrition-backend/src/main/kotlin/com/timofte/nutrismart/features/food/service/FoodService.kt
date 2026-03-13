package com.timofte.nutrismart.features.food.service

import com.timofte.nutrismart.features.food.model.Food
import com.timofte.nutrismart.features.food.repository.FoodRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class FoodService(private val foodRepository: FoodRepository) {
    fun searchFoods(query: String): List<String> {
        if (query.isBlank()) return emptyList()
        return foodRepository.findByNameContainingIgnoreCaseOrderByNameAsc(query).map { it.name }
    }

    @Transactional
    fun saveFood(name: String): Food {
        return foodRepository.save(Food(name = name))
    }
}
