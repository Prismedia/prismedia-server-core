package com.prismedia.server.dto.auth

import com.prismedia.server.dto.user.UserDto

data class AuthResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val user: UserDto
)
