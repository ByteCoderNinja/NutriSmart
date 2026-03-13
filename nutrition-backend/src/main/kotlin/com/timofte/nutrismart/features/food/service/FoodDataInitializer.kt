package com.timofte.nutrismart.features.food.service

import com.timofte.nutrismart.features.food.model.Food
import com.timofte.nutrismart.features.food.repository.FoodRepository
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.io.BufferedReader
import java.io.InputStreamReader

@Component
class FoodDataInitializer(private val foodRepository: FoodRepository) {

    @EventListener(ApplicationReadyEvent::class)
    @Transactional
    fun init() {
        if (foodRepository.count() > 0) {
            return
        }

        val startTime = System.currentTimeMillis()

        try {
            val resource = ClassPathResource("ingredients.csv")

            val uniqueFoods = mutableSetOf<String>()

            BufferedReader(InputStreamReader(resource.inputStream)).use { reader ->
                reader.forEachLine { line ->
                    val foodName = line.trim()

                    if (foodName.isNotBlank() && !foodName.startsWith("//") && foodName.length > 1) {
                        uniqueFoods.add(foodName)
                    }
                }
            }

            val foodsToSave = uniqueFoods.map { Food(name = it) }

            foodsToSave.chunked(500).forEach { batch ->
                foodRepository.saveAll(batch)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}