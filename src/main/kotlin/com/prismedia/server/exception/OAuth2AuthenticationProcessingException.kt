package com.prismedia.server.exception

import org.springframework.security.core.AuthenticationException

class OAuth2AuthenticationProcessingException(message: String) : AuthenticationException(message)
