package com.timofte.nutrismart.common.exception

import com.timofte.nutrismart.common.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.security.authentication.BadCredentialsException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(AppException::class)
    fun handleAppException(e: AppException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity
            .status(e.status)
            .body(ApiResponse.error(e.message))
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(e: BadCredentialsException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity
            .status(401)
            .body(ApiResponse.error("Invalid email or password"))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity
            .status(500)
            .body(ApiResponse.error("An unexpected error occurred: ${e.message}"))
    }
}
