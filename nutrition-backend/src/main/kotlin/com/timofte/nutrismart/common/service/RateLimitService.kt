package com.timofte.nutrismart.common.service

import com.timofte.nutrismart.common.exception.RateLimitExceededException
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Service
class RateLimitService {

    private val loginBuckets = ConcurrentHashMap<String, Bucket>()
    private val emailBuckets = ConcurrentHashMap<String, Bucket>()
    private val planBuckets = ConcurrentHashMap<Long, Bucket>()

    // 5 attempts per minute per IP
    private fun createLoginBucket(): Bucket {
        val limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1)))
        return Bucket.builder().addLimit(limit).build()
    }

    // 3 attempts per 5 minutes per IP or Email
    private fun createEmailBucket(): Bucket {
        val limit = Bandwidth.classic(3, Refill.intervally(3, Duration.ofMinutes(5)))
        return Bucket.builder().addLimit(limit).build()
    }

    // 5 plans generated per day per User
    private fun createPlanBucket(): Bucket {
        val limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofDays(1)))
        return Bucket.builder().addLimit(limit).build()
    }

    fun tryConsumeLogin(ip: String) {
        val bucket = loginBuckets.computeIfAbsent(ip) { createLoginBucket() }
        if (!bucket.tryConsume(1)) {
            throw RateLimitExceededException("Too many authentication attempts. Please wait.")
        }
    }

    fun tryConsumeEmail(key: String) {
        val bucket = emailBuckets.computeIfAbsent(key) { createEmailBucket() }
        if (!bucket.tryConsume(1)) {
            throw RateLimitExceededException("Too many requested codes. Please wait.")
        }
    }

    fun tryConsumePlanGeneration(userId: Long) {
        val bucket = planBuckets.computeIfAbsent(userId) { createPlanBucket() }
        if (!bucket.tryConsume(1)) {
            throw RateLimitExceededException("You have reached the limit of 5 nutritional plans per day.")
        }
    }
}
