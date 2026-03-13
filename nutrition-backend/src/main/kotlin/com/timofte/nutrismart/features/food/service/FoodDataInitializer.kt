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
            // MEAT & POULTRY
            "Beef", "Pork", "Chicken", "Lamb", "Turkey", "Duck", "Rabbit", "Venison", "Veal",
            "Goose", "Quail", "Organ Meats", "Liver", "Bacon", "Ham", "Salami", "Pepperoni", "Sausage",

            // FISH & SEAFOOD
            "Fish", "Salmon", "Tuna", "White Fish", "Trout", "Cod", "Haddock", "Halibut", "Tilapia",
            "Sardine", "Mackerel", "Anchovy", "Sea Bass", "Snapper", "Mahi Mahi",
            "Seafood", "Shrimp", "Squid", "Mussels", "Crab", "Lobster", "Crawfish", "Oyster", "Clam", "Scallop", "Octopus", "Calamari",

            // DAIRY & EGGS
            "Eggs", "Duck Eggs", "Quail Eggs",
            "Milk", "Almond Milk", "Oat Milk", "Soy Milk", "Coconut Milk",
            "Yogurt", "Greek Yogurt", "Kefir",
            "Butter", "Ghee", "Cream", "Sour Cream", "Cream Cheese", "Heavy Cream",
            "Cheese", "Cottage Cheese", "Hard Cheese", "Ricotta", "Mozzarella", "Cheddar", "Parmesan", "Gouda", "Brie", "Feta", "Blue Cheese", "Goat Cheese", "Swiss Cheese", "Halloumi",

            // VEGETABLES & GREENS
            "Spinach", "Broccoli", "Cauliflower", "Cabbage", "Red Cabbage", "Bok Choy", "Brussels Sprouts",
            "Mushrooms", "Shiitake Mushroom", "Portobello Mushroom",
            "Onions", "Red Onion", "Shallot", "Leek", "Garlic",
            "Tomatoes", "Eggplant", "Zucchini", "Bell Peppers", "Jalapeno", "Chili Pepper",
            "Olives", "Carrots", "Celery", "Asparagus", "Peas", "Corn",
            "Sweet Potatoes", "Potato", "Nettles",
            "Kale", "Arugula", "Lettuce", "Romaine Lettuce", "Iceberg Lettuce", "Swiss Chard",
            "Radish", "Beetroot", "Turnip", "Parsnip", "Pumpkin", "Butternut Squash", "Acorn Squash", "Artichoke", "Green Beans", "Okra", "Fennel",

            // FRUITS
            "Apples", "Bananas", "Citrus Fruits", "Orange", "Lemon", "Lime", "Grapefruit", "Tangerine",
            "Berries", "Strawberry", "Blueberry", "Raspberry", "Blackberry", "Cranberry",
            "Melons", "Watermelon", "Cantaloupe", "Honeydew",
            "Grapes", "Cherry", "Peach", "Plum", "Apricot", "Nectarine", "Pear",
            "Pineapple", "Mango", "Papaya", "Guava", "Kiwi", "Fig", "Date", "Pomegranate", "Passion Fruit", "Dragon Fruit", "Lychee", "Persimmon", "Starfruit",
            "Avocado", "Coconut", "Raisins",

            // LEGUMES (BEANS) & SOY
            "Beans", "Black Beans", "Kidney Beans", "Pinto Beans", "Navy Beans", "Cannellini Beans",
            "Lentils", "Green Lentils", "Red Lentils",
            "Chickpeas", "Soybeans", "Edamame", "Soy", "Tofu", "Tempeh",

            // NUTS & SEEDS
            "Nuts", "Peanuts", "Almonds", "Walnuts", "Pecans", "Cashews", "Pistachios", "Macadamia Nuts", "Brazil Nuts", "Pine Nuts", "Hazelnuts",
            "Peanut Butter", "Almond Butter",
            "Chia Seeds", "Flax Seeds", "Hemp Seeds", "Sunflower Seeds", "Pumpkin Seeds", "Sesame Seeds", "Poppy Seeds",

            // GRAINS, PASTA & BREAD
            "Quinoa", "Brown Rice", "White Rice", "Basmati Rice", "Jasmine Rice",
            "Oats", "Rolled Oats", "Buckwheat", "Barley", "Rye", "Wheat", "Millet", "Bulgur", "Couscous", "Farro",
            "Flour", "Whole Wheat Flour", "Cornmeal",
            "Spaghetti", "Macaroni", "Penne", "Fusilli", "Noodles", "Rice Noodles",
            "Bread", "Whole Wheat Bread", "Sourdough Bread", "Bagel", "Croissant", "Tortilla", "Pita Bread",

            // HERBS, SPICES & CONDIMENTS
            "Cilantro", "Dill", "Parsley", "Mint", "Basil", "Oregano", "Thyme", "Rosemary", "Sage", "Bay Leaves",
            "Spicy Food", "Mustard", "Dijon Mustard", "Mayonnaise", "Ketchup", "Soy Sauce",
            "Sauerkraut", "Pickles",
            "Ginger", "Turmeric", "Cinnamon", "Nutmeg", "Cumin", "Coriander", "Paprika", "Smoked Paprika", "Chili Powder", "Cayenne Pepper", "Garlic Powder", "Onion Powder",
            "Salt", "Black Pepper", "Vanilla Extract",
            "Vinegar", "Balsamic Vinegar", "Apple Cider Vinegar",

            // OILS & FATS
            "Olive Oil", "Extra Virgin Olive Oil", "Coconut Oil", "Sunflower Oil", "Canola Oil", "Avocado Oil", "Sesame Oil", "Peanut Oil", "Lard", "Tallow",

            // SWEETENERS & MISCELLANEOUS
            "Honey", "Maple Syrup", "Stevia", "Agave Nectar", "Sugar", "Brown Sugar",
            "Dark Chocolate", "Milk Chocolate", "Cocoa Powder",
            "Protein Powder", "Whey Protein", "Vegan Protein"
        ).distinct()

        val foods = foodNames.map { Food(name = it) }
        foodRepository.saveAll(foods)
    }
}
