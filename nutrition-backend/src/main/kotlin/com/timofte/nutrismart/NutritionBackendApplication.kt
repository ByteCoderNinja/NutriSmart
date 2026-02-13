package com.timofte.nutrismart

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class NutritionBackendApplication

fun main(args: Array<String>) {
    runApplication<NutritionBackendApplication>(*args)
}
