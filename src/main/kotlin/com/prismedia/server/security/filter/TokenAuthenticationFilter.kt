package com.prismedia.server.security.filter

import com.prismedia.server.security.jwt.TokenProvider
import com.prismedia.server.security.userdetails.CustomUserDetailsService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

@Component
class TokenAuthenticationFilter(
    private val tokenProvider: TokenProvider,
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(TokenAuthenticationFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val jwt = getJwtFromRequest(request)
            logger.debug("요청에서 추출한 JWT: ${if (jwt != null) "토큰 있음" else "토큰 없음"}")
            
            if (jwt != null && tokenProvider.validateToken(jwt)) {
                val userId = tokenProvider.getUserIdFromToken(jwt)
                logger.debug("JWT에서 추출한 사용자 ID: $userId")
                
                val userDetails = (userDetailsService as CustomUserDetailsService).loadUserById(userId)
                logger.debug("사용자 정보 로드 성공: ${userDetails.username}")
                
                val authentication = UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.authorities
                )
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                
                SecurityContextHolder.getContext().authentication = authentication
                logger.debug("SecurityContext에 인증 정보 설정 완료")
            } else if (jwt != null) {
                logger.debug("JWT 검증 실패")
            }
        } catch (ex: Exception) {
            logger.error("사용자 인증을 설정할 수 없습니다", ex)
        }
        
        filterChain.doFilter(request, response)
    }

    private fun getJwtFromRequest(request: HttpServletRequest): String? {
        logger.debug("요청에서 JWT 토큰 추출 시작")
        
        // 1. Authorization 헤더에서 토큰 확인
        val bearerToken = request.getHeader("Authorization")
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            logger.debug("Authorization 헤더에서 토큰 발견")
            return bearerToken.substring(7)
        }
        
        // 2. 쿠키에서 토큰 확인
        val cookies = request.cookies
        if (cookies != null) {
            logger.debug("요청에 쿠키 존재: ${cookies.size}개")
            for (cookie in cookies) {
                logger.debug("쿠키 확인: ${cookie.name} = ${cookie.value.take(10)}...")
                if (cookie.name == "accessToken") {
                    logger.debug("accessToken 쿠키에서 토큰 발견")
                    return cookie.value
                }
            }
        } else {
            logger.debug("요청에 쿠키가 없음")
        }
        
        // 3. 요청 파라미터에서 토큰 확인 (테스트용)
        val tokenParam = request.getParameter("token")
        if (StringUtils.hasText(tokenParam)) {
            logger.debug("요청 파라미터에서 토큰 발견")
            return tokenParam
        }
        
        logger.debug("요청에서 토큰을 찾을 수 없음 (헤더 및 쿠키)")
        return null
    }
}
