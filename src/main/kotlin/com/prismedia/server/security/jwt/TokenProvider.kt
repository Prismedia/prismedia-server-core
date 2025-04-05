package com.prismedia.server.security.jwt

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SecurityException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.Key
import java.util.*
import javax.crypto.SecretKey
import com.prismedia.server.security.userdetails.UserPrincipal
import com.prismedia.server.repository.RefreshTokenRepository
import com.prismedia.server.domain.token.RefreshToken

@Component
class TokenProvider {
    private val logger = LoggerFactory.getLogger(TokenProvider::class.java)

    @Value("\${app.auth.token-secret}")
    private lateinit var tokenSecret: String

    @Value("\${app.auth.token-expiration-ms}")
    private var tokenExpirationMs: Long = 0
    
    @Value("\${app.auth.refresh-token-expiration-ms:604800000}") // 기본값 7일
    private var refreshTokenExpirationMs: Long = 0
    
    @Autowired
    private lateinit var refreshTokenRepository: RefreshTokenRepository

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
    
    // 액세스 토큰과 리프레시 토큰 쌍 생성
    fun createTokenPair(authentication: Authentication): TokenPair {
        val userPrincipal = authentication.principal as UserPrincipal
        val now = Date()
        
        // 액세스 토큰 생성 (짧은 수명)
        val accessTokenExpiry = Date(now.time + tokenExpirationMs)
        val accessToken = Jwts.builder()
            .setSubject(userPrincipal.id.toString())
            .setIssuedAt(now)
            .setExpiration(accessTokenExpiry)
            .signWith(getSigningKey())
            .compact()
        
        // 리프레시 토큰 생성 (긴 수명)
        val refreshTokenExpiry = Date(now.time + refreshTokenExpirationMs)
        val refreshToken = Jwts.builder()
            .setSubject(userPrincipal.id.toString())
            .setIssuedAt(now)
            .setExpiration(refreshTokenExpiry)
            .signWith(getSigningKey())
            .compact()
        
        // 리프레시 토큰을 데이터베이스에 저장
        saveRefreshToken(userPrincipal.id, refreshToken, refreshTokenExpiry)
        
        return TokenPair(accessToken, refreshToken, tokenExpirationMs, refreshTokenExpirationMs)
    }
    
    // 리프레시 토큰 저장
    private fun saveRefreshToken(userId: Long, refreshToken: String, expiryDate: Date) {
        val refreshTokenEntity = RefreshToken(
            userId = userId,
            token = refreshToken,
            expiryDate = expiryDate
        )
        refreshTokenRepository.save(refreshTokenEntity)
    }
    
    // 리프레시 토큰으로 새 액세스 토큰 생성
    fun refreshAccessToken(refreshToken: String): String? {
        if (!validateToken(refreshToken)) {
            return null
        }
        
        val userId = getUserIdFromToken(refreshToken)
        val refreshTokenEntity = refreshTokenRepository.findByUserIdAndToken(userId, refreshToken)
            .orElse(null) ?: return null
            
        // 토큰이 만료되었는지 확인
        if (refreshTokenEntity.expiryDate.before(Date())) {
            refreshTokenRepository.delete(refreshTokenEntity)
            return null
        }
        
        // 새 액세스 토큰 생성
        val now = Date()
        val expiryDate = Date(now.time + tokenExpirationMs)
        
        return Jwts.builder()
            .setSubject(userId.toString())
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
            logger.debug("토큰 검증 시작: ${authToken.take(10)}...")
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(authToken)
            logger.debug("토큰 검증 성공")
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

// 토큰 쌍을 담는 데이터 클래스
data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpirationMs: Long,
    val refreshTokenExpirationMs: Long
)
