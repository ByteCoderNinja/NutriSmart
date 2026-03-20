package com.timofte.nutrismart.features.auth.service

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Collections
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken

@Service
class GoogleAuthService(
    @Value("\${google.client.id}") private val webClientId: String
) {
    private val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance())
        .setAudience(Collections.singletonList(webClientId))
        .build()

    fun verifyToken(idTokenString: String): GoogleIdToken.Payload? {
        return try {
            val idToken = verifier.verify(idTokenString)
            idToken?.payload
        } catch (e: Exception) {
            null
        }
    }
}