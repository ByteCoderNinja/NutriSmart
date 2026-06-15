package com.timofte.nutrismart.common.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
class RateLimitExceededException(message: String) : RuntimeException(message)
