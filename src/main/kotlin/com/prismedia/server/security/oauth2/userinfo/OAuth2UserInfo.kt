package com.prismedia.server.security.oauth2.userinfo

abstract class OAuth2UserInfo(
    val attributes: Map<String, Any>
) {
    abstract fun getId(): String?
    abstract fun getName(): String?
    abstract fun getEmail(): String?
    abstract fun getImageUrl(): String?
}

class GoogleOAuth2UserInfo(attributes: Map<String, Any>) : OAuth2UserInfo(attributes) {
    override fun getId(): String? {
        return attributes["sub"] as? String
    }

    override fun getName(): String? {
        return attributes["name"] as? String
    }

    override fun getEmail(): String? {
        return attributes["email"] as? String
    }

    override fun getImageUrl(): String? {
        return attributes["picture"] as? String
    }
}
