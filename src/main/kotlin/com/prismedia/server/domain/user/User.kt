package com.prismedia.server.domain.user

import com.prismedia.server.domain.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "users", uniqueConstraints = [
    UniqueConstraint(columnNames = ["email"])
])
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    
    @Column(nullable = false)
    var name: String,
    
    @Column(nullable = false)
    var email: String,
    
    var imageUrl: String? = null,
    
    @Column(nullable = false)
    var emailVerified: Boolean = false,
    
    var password: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var provider: AuthProvider,
    
    var providerId: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role = Role.ROLE_USER
    
) : BaseEntity() {
    
    // OAuth2 정보로 사용자 업데이트
    fun update(name: String, imageUrl: String?): User {
        this.name = name
        this.imageUrl = imageUrl
        return this
    }
}
