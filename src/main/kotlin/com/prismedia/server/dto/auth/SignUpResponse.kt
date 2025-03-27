package com.prismedia.server.dto.auth

import java.time.LocalDateTime

data class SignUpResponse(
    val id: Long,
    val name: String,
    val email: String,
    val role: String,
    val createdAt: LocalDateTime
)
