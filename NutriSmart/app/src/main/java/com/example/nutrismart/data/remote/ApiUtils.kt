package com.example.nutrismart.data.remote

import org.json.JSONObject
import retrofit2.Response

fun Response<*>.getErrorMessage(defaultMessage: String = "An unexpected error occurred"): String {
    return try {
        val errorString = this.errorBody()?.string()
        if (!errorString.isNullOrEmpty()) {
            val jsonObject = JSONObject(errorString)
            if (jsonObject.has("message")) {
                jsonObject.getString("message")
            } else {
                defaultMessage
            }
        } else {
            defaultMessage
        }
    } catch (e: Exception) {
        defaultMessage
    }
}
