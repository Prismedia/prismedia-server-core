package com.prismedia.server.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "news_items")
class NewsItem(
    @Id
    @Column(length = 100)
    val id: String,

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false, length = 500)
    var preview: String,

    @Column(name = "image_url")
    var imageUrl: String? = null,

    @Column(name = "source_url")
    var sourceUrl: String? = null,

    @Column(name = "source_name")
    var sourceName: String? = null,

    var category: String? = null,

    @Column(name = "left_percent", nullable = false)
    var leftPercent: Double,

    @Column(name = "center_percent", nullable = false)
    var centerPercent: Double,

    @Column(name = "right_percent", nullable = false)
    var rightPercent: Double,

    @Column(name = "published_date")
    var date: LocalDateTime? = null,

    @Column(name = "source_count")
    var sourceCount: Int? = null,

    var source: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
