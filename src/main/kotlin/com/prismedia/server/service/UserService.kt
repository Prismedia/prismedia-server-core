package com.prismedia.server.service

import com.prismedia.server.domain.user.AuthProvider
import com.prismedia.server.domain.user.Role
import com.prismedia.server.domain.user.User
import com.prismedia.server.dto.auth.SignUpRequest
import com.prismedia.server.dto.auth.SignUpResponse
import com.prismedia.server.exception.BadRequestException
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
     * 회원가입 처리
     */
    @Transactional
    fun registerUser(signUpRequest: SignUpRequest): SignUpResponse {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(signUpRequest.email)) {
            throw BadRequestException("해당 이메일은 이미 사용 중입니다.")
        }

        // 사용자 엔티티 생성
        val user = User(
            name = signUpRequest.name,
            email = signUpRequest.email,
            password = passwordEncoder.encode(signUpRequest.password),
            provider = AuthProvider.LOCAL,
            emailVerified = false,
            role = Role.ROLE_USER
        )

        // 저장 및 응답 생성
        val savedUser = userRepository.save(user)
        
        return SignUpResponse(
            id = savedUser.id,
            name = savedUser.name,
            email = savedUser.email,
            role = savedUser.role.name,
            createdAt = savedUser.createdAt
        )
    }
    
    /**
     * 이메일로 사용자 조회
     */
    @Transactional(readOnly = true)
    fun getUserByEmail(email: String): User? {
        return userRepository.findByEmail(email).orElse(null)
    }
}
