package com.prismedia.server.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "news_items")
class NewsItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

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

    @Enumerated(EnumType.STRING)
    @Column(name = "political_bias")
    var politicalBias: PoliticalBias,

    @Column(name = "published_date")
    var date: LocalDateTime? = null,

    @Column(name = "source_count")
    var sourceCount: Int? = null,

    var source: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cluster_id")
    var newsCluster: NewsCluster? = null
) : BaseEntity()
