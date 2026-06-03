package com.timofte.nutrismart.features.user.service

import com.timofte.nutrismart.features.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class UserCleanupScheduler(
    private val userRepository: UserRepository
) {
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    fun deleteGhostAccounts() {
        val now = LocalDateTime.now()

        val deletedCount = userRepository.deleteByIsVerifiedFalseAndVerificationCodeExpiresAtBefore(now)

        println("Cleanup finished! Deleted $deletedCount ghost accounts.")
    }
}