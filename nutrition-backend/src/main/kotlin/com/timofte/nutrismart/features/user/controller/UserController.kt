package com.timofte.nutrismart.features.user.controller

import com.timofte.nutrismart.features.user.model.UserEntity
import com.timofte.nutrismart.features.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @PostMapping
    fun createUser(@RequestBody user: UserEntity): ResponseEntity<UserEntity> {
        val savedUser = userService.saveUser(user)
        return ResponseEntity.ok(savedUser)
    }

    @GetMapping("/{userId}")
    fun getUser(@PathVariable userId: Long): ResponseEntity<UserEntity> {
        return try {
            val user = userService.getUser(userId)
            ResponseEntity.ok(user)
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{userId}")
    fun updateUser(@PathVariable userId: Long, @RequestBody user: UserEntity): ResponseEntity<UserEntity> {
        return try {
            val updatedUser = userService.updateUser(userId, user)
            ResponseEntity.ok(updatedUser)
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{userId}")
    fun deleteUser(@PathVariable userId: Long): ResponseEntity<Void> {
        return try {
            userService.deleteUser(userId)
            ResponseEntity.noContent().build()
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }
}