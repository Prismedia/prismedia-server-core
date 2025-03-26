package com.prismedia.server.security.userdetails

import com.prismedia.server.domain.user.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User

class UserPrincipal(
    val id: Long,
    private val email: String,
    private val password: String?,
    private val authorities: Collection<GrantedAuthority>,
    private val attributes: MutableMap<String, Any> = mutableMapOf()
) : OAuth2User, UserDetails {

    override fun getName(): String = id.toString()

    override fun getAttributes(): MutableMap<String, Any> = attributes

    override fun getAuthorities(): Collection<GrantedAuthority> = authorities

    override fun getPassword(): String? = password

    override fun getUsername(): String = email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    companion object {
        fun create(user: User): UserPrincipal {
            val authorities = listOf(SimpleGrantedAuthority(user.role.name))

            return UserPrincipal(
                id = user.id,
                email = user.email,
                password = user.password,
                authorities = authorities
            )
        }

        fun create(user: User, attributes: Map<String, Any>): UserPrincipal {
            val userPrincipal = create(user)
            userPrincipal.attributes.putAll(attributes)
            return userPrincipal
        }
    }
}
