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

    val passwordHash: String?,

    @Column(nullable = false)
    var isProfileComplete: Boolean = false,

    var dateOfBirth: LocalDate? = null,

    var height: Double? = null,

    var weight: Double? = null,

    var targetWeight: Double? = null,

    @Enumerated(EnumType.STRING)
    var gender: Gender? = null,

    @Enumerated(EnumType.STRING)
    var activityLevel: ActivityLevel? = null,

    var maxDailyBudget: Double? = null,

    @ElementCollection(targetClass = DietaryPreference::class)
    @CollectionTable(name = "user_dietary_preferences", joinColumns = [JoinColumn(name = "user_id")])
    @Enumerated(EnumType.STRING)
    var dietaryPreferences: Set<DietaryPreference> = emptySet(),

    @ElementCollection(targetClass = MedicalCondition::class)
    @CollectionTable(name = "user_medical_conditions", joinColumns = [JoinColumn(name = "user_id")])
    @Enumerated(EnumType.STRING)
    var medicalConditions: Set<MedicalCondition> = emptySet(),

    var targetCalories: Int? = null,

    @Column(nullable = false)
    var isImperial: Boolean = false,

    @Enumerated(EnumType.STRING)
    var currency: Currency = Currency.RON
)