package com.timofte.nutrismart.features.user.model

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    val username: String,

    val passwordHash: String,

    @Column(nullable = false)
    val dateOfBirth: LocalDate,

    var height: Double = 0.0,
    var weight: Double = 0.0,

    @Column(nullable = false)
    var targetWeight: Double = 0.0,

    @Enumerated(EnumType.STRING)
    var gender: Gender = Gender.MALE,

    @Enumerated(EnumType.STRING)
    var activityLevel: ActivityLevel = ActivityLevel.SEDENTARY,

    var maxDailyBudget: Double = 0.0,

    @ElementCollection(targetClass = DietaryPreference::class)
    @CollectionTable(name = "user_dietary_preferences", joinColumns = [JoinColumn(name = "user_id")])
    @Enumerated(EnumType.STRING)
    var dietaryPreferences: Set<DietaryPreference> = setOf(DietaryPreference.STANDARD),

    @ElementCollection(targetClass = MedicalCondition::class)
    @CollectionTable(name = "user_medical_conditions", joinColumns = [JoinColumn(name = "user_id")])
    @Enumerated(EnumType.STRING)
    var medicalConditions: Set<MedicalCondition> = setOf(MedicalCondition.NONE),

    var targetCalories: Int = 0
)