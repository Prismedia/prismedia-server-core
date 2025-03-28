package com.prismedia.server.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController {

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
}
