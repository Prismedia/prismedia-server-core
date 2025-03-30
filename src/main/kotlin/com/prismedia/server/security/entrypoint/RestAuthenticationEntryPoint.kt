package com.prismedia.server.security.entrypoint

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

/**
 * RestAuthenticationEntryPoint
 *
 * Auth 관련 Global Handler를 대신하는 제어 클래스
 */
@Component
class RestAuthenticationEntryPoint : AuthenticationEntryPoint {
    
    private val logger = LoggerFactory.getLogger(RestAuthenticationEntryPoint::class.java)
    private val objectMapper = ObjectMapper()
    
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        logger.error("인증되지 않은 요청: {}", authException.message)
        
        response.contentType = "application/json"
        response.status = HttpStatus.UNAUTHORIZED.value()
        
        val errorBody = mapOf(
            "error" to "인증 실패",
            "message" to (authException.message ?: "인증이 필요합니다"),
            "status" to HttpStatus.UNAUTHORIZED.value()
        )
        
        objectMapper.writeValue(response.outputStream, errorBody)
    }
}
