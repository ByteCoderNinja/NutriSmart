package com.timofte.nutrismart.infrastructure.mail

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class EmailService(
    @Value("\${brevo.api.key}") private val apiKey: String
) {
    private val restTemplate = RestTemplate()
    private val brevoUrl = "https://api.brevo.com/v3/smtp/email"

    @Async
    fun sendVerificationCode(toEmail: String, code: String) {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set("api-key", apiKey)

        val body = mapOf(
            "sender" to mapOf("name" to "NutriSmart", "email" to "nutrismart.app.dev@gmail.com"),
            "to" to listOf(mapOf("email" to toEmail)),
            "subject" to "NutriSmart - Verification Code",
            "textContent" to "Hello!\n\nYour verification code is: $code\n\nThis code expires in 15 minutes."
        )

        val entity = HttpEntity(body, headers)

        try {
            val response = restTemplate.postForEntity(brevoUrl, entity, String::class.java)
            if (response.statusCode.is2xxSuccessful) {
                println("INFO: Verification email sent successfully to $toEmail via Brevo API")
            } else {
                println("ERROR: Brevo API returned status: ${response.statusCode}")
            }
        } catch (e: Exception) {
            println("ERROR: Failed to send email via HTTP. Exception: ${e.message}")
            println("DEBUG: Verification code for $toEmail is: $code")
        }
    }
}