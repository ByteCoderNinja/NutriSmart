package com.example.nutrismart.data

object UserSession {
    // In-memory cache for the current session (backed by SessionManager on disk)
    var currentUserId: Long = -1L
    var token: String = ""
    var isGoogleUser: Boolean = false

    fun clear() {
        currentUserId = -1L
        token = ""
        isGoogleUser = false
    }
}