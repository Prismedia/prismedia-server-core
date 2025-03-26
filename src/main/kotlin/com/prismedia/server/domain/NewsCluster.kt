package com.prismedia.server.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "news_clusters")
class NewsCluster(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var topic: String,

    @Column(name = "topic_description", length = 500)
    var topicDescription: String? = null,

    @Column(name = "representative_image_url")
    var representativeImageUrl: String? = null,

    @Column(name = "keywords", length = 255)
    var keywords: String? = null,

    @Column(name = "left_percent", nullable = false)
    var leftPercent: Double = 0.0,

    @Column(name = "center_left_percent", nullable = false)
    var centerLeftPercent: Double = 0.0,

    @Column(name = "center_percent", nullable = false)
    var centerPercent: Double = 0.0,

    @Column(name = "center_right_percent", nullable = false)
    var centerRightPercent: Double = 0.0,

    @Column(name = "right_percent", nullable = false)
    var rightPercent: Double = 0.0,

    @Column(name = "article_count", nullable = false)
    var articleCount: Int = 0,

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "cluster_id")
    var newsItems: MutableList<NewsItem> = mutableListOf()
) : BaseEntity() {
    // 뉴스 아이템 추가 시 자동으로 관련 통계 업데이트
    fun addNewsItem(newsItem: NewsItem) {
        newsItems.add(newsItem)
        newsItem.newsCluster = this
        articleCount = newsItems.size
        
        // 정치 성향 통계 업데이트
        updatePoliticalPercentages()
    }
    
    private fun updatePoliticalPercentages() {
        var leftCount = 0
        var centerLeftCount = 0
        var centerCount = 0
        var centerRightCount = 0
        var rightCount = 0
        
        newsItems.forEach { item ->
            when (item.politicalBias) {
                PoliticalBias.LEFT -> leftCount++
                PoliticalBias.CENTER_LEFT -> centerLeftCount++
                PoliticalBias.CENTER -> centerCount++
                PoliticalBias.CENTER_RIGHT -> centerRightCount++
                PoliticalBias.RIGHT -> rightCount++
            }
        }
        
        val totalCount = newsItems.size.toDouble()
        if (totalCount > 0) {
            leftPercent = leftCount / totalCount * 100
            centerLeftPercent = centerLeftCount / totalCount * 100
            centerPercent = centerCount / totalCount * 100
            centerRightPercent = centerRightCount / totalCount * 100
            rightPercent = rightCount / totalCount * 100
        } else {
            leftPercent = 0.0
            centerLeftPercent = 0.0
            centerPercent = 0.0
            centerRightPercent = 0.0
            rightPercent = 0.0
        }
    }
    
    // 클러스터 정보 업데이트
    fun updateClusterInfo() {
        updatePoliticalPercentages()
    }
}
