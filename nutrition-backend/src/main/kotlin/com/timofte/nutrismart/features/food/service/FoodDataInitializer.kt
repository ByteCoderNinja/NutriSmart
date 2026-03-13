package com.timofte.nutrismart.features.food.service

import com.timofte.nutrismart.features.food.model.Food
import com.timofte.nutrismart.features.food.repository.FoodRepository
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class FoodDataInitializer(private val foodRepository: FoodRepository) {

    @EventListener(ApplicationReadyEvent::class)
    @Transactional
    fun init() {
        if (foodRepository.count() > 0) return

        val foodNames = listOf(
            "Beef", "Pork", "Chicken", "Lamb", "Turkey", "Duck", "Rabbit", "Venison", "Organ Meats",
            "Fish", "Salmon", "Tuna", "White Fish", "Seafood", "Shrimp", "Squid", "Mussels",
            "Eggs", "Milk", "Yogurt", "Butter", "Cheese", "Cottage Cheese", "Hard Cheese", "Cream",
            "Spinach", "Broccoli", "Cauliflower", "Cabbage", "Mushrooms", "Onions", "Garlic", "Tomatoes", "Eggplant", "Zucchini", "Bell Peppers", "Olives", "Carrots", "Celery", "Asparagus", "Peas", "Corn", "Sweet Potatoes", "Nettles",
            "Apples", "Bananas", "Citrus Fruits", "Berries", "Melons", "Avocado", "Coconut", "Raisins",
            "Beans", "Lentils", "Chickpeas", "Nuts", "Peanuts", "Almonds", "Walnuts", "Soy", "Tofu",
            "Cilantro", "Dill", "Parsley", "Mint", "Spicy Food", "Mustard", "Mayonnaise",
            "Sauerkraut", "Pickles", "Ginger", "Turmeric", "Cinnamon", "Basil", "Oregano", "Thyme",
            "Rosemary", "Kale", "Arugula", "Lettuce", "Radish", "Beetroot", "Pumpkin", "Quinoa",
            "Brown Rice", "White Rice", "Oats", "Buckwheat", "Barley", "Rye", "Wheat", "Honey",
            "Maple Syrup", "Stevia", "Olive Oil", "Coconut Oil", "Sunflower Oil", "Ghee"
        ).distinct()

        val foods = foodNames.map { Food(name = it) }
        foodRepository.saveAll(foods)
    }
}
