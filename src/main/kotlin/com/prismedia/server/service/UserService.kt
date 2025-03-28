package com.prismedia.server.service

import com.prismedia.server.domain.user.User
import com.prismedia.server.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    
    /**
     * 이메일로 사용자 조회
     */
    @Transactional(readOnly = true)
    fun getUserByEmail(email: String): User? {
        return userRepository.findByEmail(email).orElse(null)
    }
}
