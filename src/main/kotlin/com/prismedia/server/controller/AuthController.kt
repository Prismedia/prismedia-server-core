package com.prismedia.server.controller

import com.prismedia.server.domain.user.User
import com.prismedia.server.repository.UserRepository
import com.prismedia.server.security.jwt.TokenProvider
import com.prismedia.server.security.userdetails.UserPrincipal
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.beans.factory.annotation.Value

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val tokenProvider: TokenProvider,
    private val userRepository: UserRepository
) {
    @Value("\${app.auth.token-expiration-ms}")
    private var tokenExpirationMs: Long = 0

    /**
     * 인증 정보 API
     * 사용 가능한 인증 방법에 대한 정보를 제공합니다.
     */
    @GetMapping("/info")
    fun getAuthInfo(): ResponseEntity<Map<String, Any>> {
        val authInfo = mapOf(
            "message" to "Google OAuth2 로그인만 지원합니다.",
            "loginUrl" to "/oauth2/authorize/google",
            "callbackUrl" to "/oauth2/callback/google"
        )
        return ResponseEntity.ok(authInfo)
    }
    
    /**
     * 현재 로그인한 사용자 정보 API
     * 현재 인증된 사용자의 정보를 반환합니다.
     */
    @GetMapping("/me")
    fun getCurrentUser(@AuthenticationPrincipal userPrincipal: UserPrincipal?): ResponseEntity<Any> {
        if (userPrincipal == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "인증되지 않은 사용자입니다."))
        }
        
        val user = userRepository.findById(userPrincipal.id)
            .orElseThrow { RuntimeException("사용자를 찾을 수 없습니다: ${userPrincipal.id}") }
        
        val userInfo = mapOf(
            "id" to user.id,
            "name" to user.name,
            "email" to user.email,
            "imageUrl" to user.imageUrl,
            "role" to user.role.name
        )
        
        return ResponseEntity.ok(userInfo)
    }
    
    /**
     * 토큰 갱신 API
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다.
     */
    @PostMapping("/refresh-token")
    fun refreshToken(request: HttpServletRequest, response: HttpServletResponse): ResponseEntity<Map<String, Any>> {
        // 쿠키에서 리프레시 토큰 추출
        val refreshToken = request.cookies?.find { it.name == "refreshToken" }?.value
        
        if (refreshToken.isNullOrEmpty()) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "리프레시 토큰이 없습니다."))
        }
        
        // 리프레시 토큰으로 새 액세스 토큰 생성
        val newAccessToken = tokenProvider.refreshAccessToken(refreshToken)
        
        if (newAccessToken == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "유효하지 않은 리프레시 토큰입니다."))
        }
        
        // 새 액세스 토큰을 쿠키에 설정
        val cookie = Cookie("accessToken", newAccessToken)
        cookie.path = "/"
        cookie.isHttpOnly = true
        cookie.secure = false  // 개발 환경에서는 false, 프로덕션에서는 true로 설정
        cookie.maxAge = (tokenExpirationMs / 1000).toInt()
        response.addCookie(cookie)
        
        return ResponseEntity.ok(mapOf("message" to "액세스 토큰이 갱신되었습니다."))
    }
    
    /**
     * 로그아웃 API
     * 클라이언트의 쿠키에서 토큰을 제거합니다.
     */
    @PostMapping("/logout")
    fun logout(request: HttpServletRequest, response: HttpServletResponse): ResponseEntity<Map<String, Any>> {
        // 액세스 토큰 쿠키 제거
        val accessTokenCookie = Cookie("accessToken", "")
        accessTokenCookie.path = "/"
        accessTokenCookie.maxAge = 0
        response.addCookie(accessTokenCookie)
        
        // 리프레시 토큰 쿠키 제거
        val refreshTokenCookie = Cookie("refreshToken", "")
        refreshTokenCookie.path = "/"
        refreshTokenCookie.maxAge = 0
        response.addCookie(refreshTokenCookie)
        
        return ResponseEntity.ok(mapOf("message" to "로그아웃 되었습니다."))
    }
}
