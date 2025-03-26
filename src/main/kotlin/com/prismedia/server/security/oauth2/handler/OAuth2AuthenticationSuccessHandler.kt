package com.prismedia.server.security.oauth2.handler

import com.prismedia.server.security.jwt.TokenProvider
import com.prismedia.server.util.CookieUtils
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

        clearAuthenticationAttributes(request, response)
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    override protected fun determineTargetUrl(
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

        val token = tokenProvider.createToken(authentication)

        return UriComponentsBuilder.fromUriString(redirectUriParam)
            .queryParam("token", token)
            .build().toUriString()
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
