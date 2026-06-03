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
        val threshold = LocalDateTime.now().minusDays(1)

        val deletedCount = userRepository.deleteByIsVerifiedFalseAndVerificationCodeExpiresAtBefore(threshold)

        println("Cleanup finished! Deleted $deletedCount ghost accounts.")
    }
}