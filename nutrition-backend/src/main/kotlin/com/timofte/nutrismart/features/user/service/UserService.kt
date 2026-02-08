package com.timofte.nutrismart.features.user.service

import com.timofte.nutrismart.features.user.model.*
import com.timofte.nutrismart.features.user.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Period

@Service
class UserService(private val userRepository: UserRepository) {

    fun calculateAndSaveUserNeeds(user: UserEntity): UserEntity {
        val age = Period.between(user.dateOfBirth, LocalDate.now()).years

        // BMR (Basal Metabolic Rate) - numarul de calorii arse de corp fara sa faci nimic
        var bmr = (10 * user.weight) + (6.25 * user.height) - (5 * age)

        // ajustare in functie de sex - la barbati aduni 5, iar la femei scazi 161
        bmr += if (user.gender == Gender.MALE) 5.0 else -161.0

        // TDEE (Total Daily Energy Expenditure)
        val tdee = bmr * user.activityLevel.multiplier

        // Decidem obiectivul
        val diff = user.targetWeight - user.weight

        val calories = when {
            diff < -1.0 -> tdee - 500 // slabire
            diff > 1.0 -> tdee + 300  // masa musculara
            else -> tdee              // mentinere
        }

        user.targetCalories = calories.toInt()

        return userRepository.save(user)
    }

    fun getUser(id: Long): UserEntity {
        return userRepository.findById(id).orElseThrow { RuntimeException("User not found") }
    }

    fun saveUser(user: UserEntity): UserEntity {
        return userRepository.save(user)
    }
}