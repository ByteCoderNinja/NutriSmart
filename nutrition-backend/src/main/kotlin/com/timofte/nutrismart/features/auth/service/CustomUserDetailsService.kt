package com.timofte.nutrismart.features.auth.service

import com.timofte.nutrismart.features.user.repository.UserRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val userEntity = userRepository.findByEmail(username)
        ?: throw UsernameNotFoundException("User not found with email: $username")

        return User.builder()
            .username(userEntity.email)
            .password(userEntity.passwordHash)
            .roles("USER")
            .build()
    }
}