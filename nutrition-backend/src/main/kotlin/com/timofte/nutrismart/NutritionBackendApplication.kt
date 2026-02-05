package com.timofte.nutrismart

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NutritionBackendApplication

fun main(args: Array<String>) {
    runApplication<NutritionBackendApplication>(*args)
}
