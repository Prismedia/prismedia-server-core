package com.prismedia.server.security.jwt

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SecurityException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.Key
import java.util.*
import javax.crypto.SecretKey
import com.prismedia.server.security.userdetails.UserPrincipal

@Component
class TokenProvider {
    private val logger = LoggerFactory.getLogger(TokenProvider::class.java)

    @Value("\${app.auth.token-secret}")
    private lateinit var tokenSecret: String

    @Value("\${app.auth.token-expiration-ms}")
    private var tokenExpirationMs: Long = 0

    // SecretKey 생성
    private fun getSigningKey(): SecretKey {
        val keyBytes = tokenSecret.toByteArray(StandardCharsets.UTF_8)
        return Keys.hmacShaKeyFor(keyBytes)
    }

    // JWT 토큰 생성
    fun createToken(authentication: Authentication): String {
        val userPrincipal = authentication.principal as UserPrincipal
        val now = Date()
        val expiryDate = Date(now.time + tokenExpirationMs)

        return Jwts.builder()
            .setSubject(userPrincipal.id.toString())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey())
            .compact()
    }

    // JWT 토큰에서 사용자 ID 추출
    fun getUserIdFromToken(token: String): Long {
        val claims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .body

        return claims.subject.toLong()
    }

    // JWT 토큰 유효성 검증
    fun validateToken(authToken: String): Boolean {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(authToken)
            return true
        } catch (ex: SecurityException) {
            logger.error("유효하지 않은 JWT 서명입니다.")
        } catch (ex: MalformedJwtException) {
            logger.error("유효하지 않은 JWT 토큰입니다.")
        } catch (ex: ExpiredJwtException) {
            logger.error("만료된 JWT 토큰입니다.")
        } catch (ex: UnsupportedJwtException) {
            logger.error("지원되지 않는 JWT 토큰입니다.")
        } catch (ex: IllegalArgumentException) {
            logger.error("JWT 클레임 문자열이 비어 있습니다.")
        }
        return false
    }
}
