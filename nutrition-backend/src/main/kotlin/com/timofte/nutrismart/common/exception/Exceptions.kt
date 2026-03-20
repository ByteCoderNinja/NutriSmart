package com.timofte.nutrismart.common.exception

import org.springframework.http.HttpStatus

sealed class AppException(
    val status: HttpStatus,
    override val message: String
) : RuntimeException(message)

class ResourceNotFoundException(message: String) : AppException(HttpStatus.NOT_FOUND, message)
class BadRequestException(message: String) : AppException(HttpStatus.BAD_REQUEST, message)
class ConflictException(message: String) : AppException(HttpStatus.CONFLICT, message)
class UnauthorizedException(message: String) : AppException(HttpStatus.UNAUTHORIZED, message)
class ForbiddenException(message: String) : AppException(HttpStatus.FORBIDDEN, message)
