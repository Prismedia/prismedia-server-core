package com.prismedia.server.domain.token

import jakarta.persistence.*
import java.util.Date

@Entity
@Table(name = "refresh_tokens")
data class RefreshToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false)
    val userId: Long,
    
    @Column(nullable = false, unique = true, length = 255)
    val token: String,
    
    @Column(nullable = false)
    val expiryDate: Date,
    
    @Column(nullable = false)
    val createdAt: Date = Date()
)
