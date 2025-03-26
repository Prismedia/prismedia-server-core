package com.prismedia.server.security.oauth2.userinfo

import com.prismedia.server.domain.user.AuthProvider

object OAuth2UserInfoFactory {
    
    fun getOAuth2UserInfo(registrationId: String, attributes: Map<String, Any>): OAuth2UserInfo {
        return when {
            registrationId.equals(AuthProvider.GOOGLE.toString(), ignoreCase = true) -> {
                GoogleOAuth2UserInfo(attributes)
            }
            else -> {
                throw RuntimeException("죄송합니다. $registrationId 로그인은 지원하지 않습니다.")
            }
        }
    }
}
