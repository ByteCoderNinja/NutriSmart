package com.timofte.nutrismart.features.user.repository

import com.timofte.nutrismart.features.user.model.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findByEmail(email: String): UserEntity?
    fun deleteByIsVerifiedFalseAndVerificationCodeExpiresAtBefore(dateTime: LocalDateTime): Long
}