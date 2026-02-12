package com.timofte.nutrismart.features.auth.service

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Collections

@Service
class GoogleAuthService(
    @Value("\${google.client.id}") val googleClientId: String
) {
    fun verifyToken(tokenString: String): GoogleIdToken.Payload? {
        val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory())
            .setAudience(Collections.singletonList(googleClientId))
            .build()

        return try {
            val idToken = verifier.verify(tokenString)
            idToken?.payload
        } catch (e: Exception) {
            println("ERROR: Google Token Verification failed: ${e.message}")
            null
        }
    }
}