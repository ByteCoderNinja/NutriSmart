package com.example.nutrismart.data

object UserSession {
    var currentUserId: Long = -1L
    var token: String = ""

    fun clear() {
        currentUserId = -1L
        token = ""
    }
}