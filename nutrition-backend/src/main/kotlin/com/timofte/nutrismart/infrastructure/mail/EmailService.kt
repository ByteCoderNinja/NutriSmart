package com.timofte.nutrismart.infrastructure.mail

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(private val mailSender: JavaMailSender) {

    fun sendVerificationCode(toEmail: String, code: String) {
        val message = SimpleMailMessage()
        message.from = "nutrismart.app.dev@gmail.com"
        message.setTo(toEmail)
        message.subject = "NutriSmart - Verification Code"
        message.text = "Hello!\n\nYour verification code is: $code\n\nThis code expires in 15 minutes."

        try {
            mailSender.send(message)
            println("INFO: Verification email sent to $toEmail")
        } catch (e: Exception) {
            println("ERROR: Failed to send email. Check SMTP configuration.")
            println("DEBUG: Verification code for $toEmail is: $code")
        }
    }
}