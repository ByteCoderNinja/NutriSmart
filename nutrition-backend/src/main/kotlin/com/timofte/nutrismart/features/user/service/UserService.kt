package com.timofte.nutrismart.features.user.service

import com.timofte.nutrismart.features.nutrition.service.NutritionService
import com.timofte.nutrismart.features.user.dto.OnboardingRequest
import com.timofte.nutrismart.features.user.model.*
import com.timofte.nutrismart.features.user.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Period

@Service
class UserService(
    private val userRepository: UserRepository,
    private val nutritionService: NutritionService
) {

    fun completeUserProfile(email: String, request: OnboardingRequest): UserEntity {
        val user = userRepository.findByEmail(email)
            ?: throw RuntimeException("User not found for email: $email")

        user.apply {
            dateOfBirth = request.dateOfBirth
            gender = request.gender
            height = request.height
            weight = request.weight
            targetWeight = request.targetWeight
            activityLevel = request.activityLevel
            maxDailyBudget = request.maxDailyBudget
            dietaryPreferences = request.dietaryPreferences
            medicalConditions = request.medicalConditions
            isImperial = request.isImperial
            currency = request.currency

            isProfileComplete = true
        }

        val savedUser = calculateAndSaveUserNeeds(user)

        try {
            nutritionService.generateAndSaveWeeklyPlan(savedUser.id)
        } catch (e: Exception) {
            println("Error while generating AI plan: ${e.message}")
        }

        return savedUser
    }

    fun calculateAndSaveUserNeeds(user: UserEntity): UserEntity {
        if (user.dateOfBirth == null || user.weight == null || user.height == null || user.gender == null) {
            return userRepository.save(user)
        }

        val dob = user.dateOfBirth!!
        val weight = user.weight!!
        val height = user.height!!
        val gender = user.gender!!
        val activity = user.activityLevel ?: ActivityLevel.SEDENTARY

        val age = Period.between(dob, LocalDate.now()).years

        var bmr = (10 * weight) + (6.25 * height) - (5 * age)

        bmr += if (gender == Gender.MALE) 5.0 else -161.0

        val tdee = bmr * activity.multiplier

        val targetWeight = user.targetWeight ?: weight
        val diff = targetWeight - weight

        val finalCalories = when {
            diff < -1.0 -> tdee - 500 // slabire
            diff > 1.0 -> tdee + 300 // masa musculara
            else -> tdee // mentinere
        }

        user.targetCalories = finalCalories.toInt()

        return userRepository.save(user)
    }


    fun getUserByEmail(email: String): UserEntity {
        return userRepository.findByEmail(email)
            ?: throw RuntimeException("User not found")
    }

    fun getUser(id: Long): UserEntity {
        return userRepository.findById(id).orElseThrow { RuntimeException("User not found") }
    }

    fun saveUser(user: UserEntity): UserEntity {
        return calculateAndSaveUserNeeds(user)
    }

    fun updateUser(id: Long, updatedData: UserEntity): UserEntity {
        val existingUser = getUser(id)

        val needsRegeneration = existingUser.weight != updatedData.weight ||
                existingUser.targetWeight != updatedData.targetWeight ||
                existingUser.dietaryPreferences != updatedData.dietaryPreferences ||
                existingUser.medicalConditions != updatedData.medicalConditions ||
                existingUser.activityLevel != updatedData.activityLevel ||
                existingUser.maxDailyBudget != updatedData.maxDailyBudget ||
                existingUser.isImperial != updatedData.isImperial ||
                existingUser.currency != updatedData.currency ||
                existingUser.height != updatedData.height

        existingUser.apply {
            weight = updatedData.weight
            height = updatedData.height
            targetWeight = updatedData.targetWeight
            activityLevel = updatedData.activityLevel
            dateOfBirth = updatedData.dateOfBirth
            dietaryPreferences = updatedData.dietaryPreferences
            medicalConditions = updatedData.medicalConditions
            maxDailyBudget = updatedData.maxDailyBudget
            isImperial = updatedData.isImperial
            currency = updatedData.currency
        }

        val savedUser = calculateAndSaveUserNeeds(existingUser)

        if (needsRegeneration) {
            try {
                nutritionService.generateAndSaveWeeklyPlan(savedUser.id)
            } catch (e: Exception) {
                println("Error while generating AI plan: ${e.message}")
            }
        }

        return savedUser
    }

    fun deleteUser(id: Long) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id)
        } else {
            throw RuntimeException("User not found")
        }
    }
}