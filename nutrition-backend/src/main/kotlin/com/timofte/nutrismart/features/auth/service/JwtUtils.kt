package com.timofte.nutrismart.features.auth.service

import org.springframework.stereotype.Component
import java.util.UUID

@Component
class JwtUtils {
    fun generateToken(email: String): String {
        return "TOKEN_" + UUID.randomUUID().toString() + "_USER_" + email
    }
}