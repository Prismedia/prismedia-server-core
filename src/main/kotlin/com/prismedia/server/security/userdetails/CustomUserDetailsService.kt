package com.prismedia.server.security.userdetails

import com.prismedia.server.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {

    @Transactional(readOnly = true)
    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email)
            .orElseThrow { UsernameNotFoundException("이메일 $email 에 해당하는 사용자를 찾을 수 없습니다.") }

        return UserPrincipal.create(user)
    }

    @Transactional(readOnly = true)
    fun loadUserById(id: Long): UserDetails {
        val user = userRepository.findById(id)
            .orElseThrow { UsernameNotFoundException("ID $id 에 해당하는 사용자를 찾을 수 없습니다.") }

        return UserPrincipal.create(user)
    }
}
