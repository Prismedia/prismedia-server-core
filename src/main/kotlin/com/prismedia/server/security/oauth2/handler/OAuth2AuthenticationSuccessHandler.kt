package com.prismedia.server.security.oauth2.handler

import com.prismedia.server.security.jwt.TokenProvider
import com.prismedia.server.util.CookieUtils
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class OAuth2AuthenticationSuccessHandler(
    private val tokenProvider: TokenProvider,
    private val cookieUtils: CookieUtils
) : SimpleUrlAuthenticationSuccessHandler() {

    @Value("\${app.oauth2.authorized-redirect-uri}")
    private lateinit var redirectUri: String
    
    @Value("\${app.auth.token-expiration-ms}")
    private var tokenExpirationMs: Long = 0
    
    @Value("\${app.auth.refresh-token-expiration-ms:604800000}") // 기본값 7일
    private var refreshTokenExpirationMs: Long = 0

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val targetUrl = determineTargetUrl(request, response, authentication)
        
        if (response.isCommitted) {
            logger.debug("응답이 이미 커밋되었습니다. $targetUrl 로 리다이렉트 할 수 없습니다")
            return
        }
        
        // 토큰 쌍 생성
        val tokenPair = tokenProvider.createTokenPair(authentication)
        logger.debug("인증 성공: 토큰 생성 완료, 액세스 토큰: ${tokenPair.accessToken.take(10)}...")
        
        try {
            // 액세스 토큰을 HttpOnly 쿠키로 설정
            val accessTokenCookie = Cookie("accessToken", tokenPair.accessToken)
            accessTokenCookie.path = "/"
            accessTokenCookie.isHttpOnly = true
            accessTokenCookie.secure = false  // 개발 환경에서는 false, 프로덕션에서는 true로 설정
            accessTokenCookie.maxAge = (tokenPair.accessTokenExpirationMs / 1000).toInt()
            response.addCookie(accessTokenCookie)
            logger.debug("액세스 토큰 쿠키 설정 완료: ${accessTokenCookie.name}, 경로: ${accessTokenCookie.path}, HttpOnly: ${accessTokenCookie.isHttpOnly}, 만료: ${accessTokenCookie.maxAge}초")
            
            // 리프레시 토큰을 HttpOnly 쿠키로 설정
            val refreshTokenCookie = Cookie("refreshToken", tokenPair.refreshToken)
            refreshTokenCookie.path = "/"
            refreshTokenCookie.isHttpOnly = true
            refreshTokenCookie.secure = false  // 개발 환경에서는 false, 프로덕션에서는 true로 설정
            refreshTokenCookie.maxAge = (tokenPair.refreshTokenExpirationMs / 1000).toInt()
            response.addCookie(refreshTokenCookie)
            logger.debug("리프레시 토큰 쿠키 설정 완료: ${refreshTokenCookie.name}, 경로: ${refreshTokenCookie.path}, HttpOnly: ${refreshTokenCookie.isHttpOnly}, 만료: ${refreshTokenCookie.maxAge}초")
            
            // SameSite 속성 설정을 위한 헤더 추가
            val accessTokenHeader = "accessToken=${tokenPair.accessToken}; Max-Age=${accessTokenCookie.maxAge}; Path=/; HttpOnly; SameSite=Lax"
            val refreshTokenHeader = "refreshToken=${tokenPair.refreshToken}; Max-Age=${refreshTokenCookie.maxAge}; Path=/; HttpOnly; SameSite=Lax"
            
            response.addHeader("Set-Cookie", accessTokenHeader)
            response.addHeader("Set-Cookie", refreshTokenHeader)
            logger.debug("쿠키 헤더 설정 완료")
            
        } catch (e: Exception) {
            logger.error("쿠키 설정 중 오류 발생: ${e.message}")
            e.printStackTrace()
        }

        clearAuthenticationAttributes(request, response)
        logger.debug("인증 속성 정리 완료, 리다이렉트 URL: $targetUrl")
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    override fun determineTargetUrl(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ): String {
        val redirectUriParam = cookieUtils.getCookie(request, "redirect_uri")
            ?.value
            ?: redirectUri
        
        logger.debug("리다이렉트 URI: $redirectUriParam")
        
        if (!isAuthorizedRedirectUri(redirectUriParam)) {
            throw IllegalArgumentException("승인되지 않은 리다이렉트 URI이므로 인증을 진행할 수 없습니다")
        }
        
        // 인증 성공 여부를 쿼리 파라미터로 추가
        val targetUrl = UriComponentsBuilder.fromUriString(redirectUriParam)
            .queryParam("auth_success", "true")
            .build()
            .toUriString()
            
        logger.debug("최종 리다이렉트 URL: $targetUrl")
        return targetUrl
    }

    protected fun clearAuthenticationAttributes(request: HttpServletRequest, response: HttpServletResponse) {
        super.clearAuthenticationAttributes(request)
        cookieUtils.deleteCookie(request, response, "redirect_uri")
    }

    private fun isAuthorizedRedirectUri(uri: String): Boolean {
        val clientRedirectUri = URI.create(uri)
        val authorizedUri = URI.create(redirectUri)
        
        return authorizedUri.host.equals(clientRedirectUri.host, ignoreCase = true) &&
                authorizedUri.port == clientRedirectUri.port
    }
}
