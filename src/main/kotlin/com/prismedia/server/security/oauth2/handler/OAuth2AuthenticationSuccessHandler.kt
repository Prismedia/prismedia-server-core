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
        
        // 액세스 토큰을 HttpOnly 쿠키로 설정
        addCookie(
            response, 
            "accessToken", 
            tokenPair.accessToken, 
            (tokenPair.accessTokenExpirationMs / 1000).toInt()
        )
        
        // 리프레시 토큰을 HttpOnly 쿠키로 설정
        addCookie(
            response, 
            "refreshToken", 
            tokenPair.refreshToken, 
            (tokenPair.refreshTokenExpirationMs / 1000).toInt()
        )

        clearAuthenticationAttributes(request, response)
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
        
        if (!isAuthorizedRedirectUri(redirectUriParam)) {
            throw IllegalArgumentException("승인되지 않은 리다이렉트 URI이므로 인증을 진행할 수 없습니다")
        }
        
        // 토큰을 URL 파라미터로 전달하지 않고 쿠키로만 전달
        return redirectUriParam
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
    
    private fun addCookie(response: HttpServletResponse, name: String, value: String, maxAge: Int) {
        val cookie = Cookie(name, value)
        cookie.path = "/"
        cookie.isHttpOnly = true
        cookie.secure = false  // 개발 환경에서는 false, 프로덕션에서는 true로 설정
        cookie.maxAge = maxAge
        response.addCookie(cookie)
    }
}
