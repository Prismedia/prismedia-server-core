package com.prismedia.server.repository

import com.prismedia.server.domain.token.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByToken(token: String): Optional<RefreshToken>
    fun findByUserIdAndToken(userId: Long, token: String): Optional<RefreshToken>
    fun deleteByUserId(userId: Long)
}
