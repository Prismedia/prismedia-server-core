package com.prismedia.server.security.oauth2.handler

import com.prismedia.server.util.CookieUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OAuth2AuthenticationFailureHandler(
    private val cookieUtils: CookieUtils
) : SimpleUrlAuthenticationFailureHandler() {

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        val redirectUriCookie = cookieUtils.getCookie(request, "redirect_uri")
        
        val targetUrl = if (redirectUriCookie != null) {
            redirectUriCookie.value
        } else {
            "/"
        }

        val redirectUri = UriComponentsBuilder.fromUriString(targetUrl)
            .queryParam("error", exception.localizedMessage)
            .build().toUriString()

        cookieUtils.deleteCookie(request, response, "redirect_uri")
        
        redirectStrategy.sendRedirect(request, response, redirectUri)
    }
}
