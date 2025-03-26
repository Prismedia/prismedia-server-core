package com.prismedia.server.dto.user

import com.prismedia.server.domain.user.AuthProvider
import com.prismedia.server.domain.user.Role
import com.prismedia.server.domain.user.User

data class UserDto(
    val id: Long = 0,
    val name: String,
    val email: String,
    val imageUrl: String? = null,
    val role: Role = Role.ROLE_USER,
    val provider: AuthProvider
) {
    companion object {
        fun fromEntity(user: User): UserDto {
            return UserDto(
                id = user.id,
                name = user.name,
                email = user.email,
                imageUrl = user.imageUrl,
                role = user.role,
                provider = user.provider
            )
        }
    }
}
