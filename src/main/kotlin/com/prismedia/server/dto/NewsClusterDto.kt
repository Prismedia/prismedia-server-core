package com.prismedia.server.dto

import com.prismedia.server.domain.NewsCluster
import java.time.format.DateTimeFormatter

data class NewsClusterDto(
    val id: Long = 0,
    val topic: String,
    val topicDescription: String? = null,
    val representativeImageUrl: String? = null,
    val keywords: String? = null,
    val leftPercent: Double = 0.0,
    val centerLeftPercent: Double = 0.0,
    val centerPercent: Double = 0.0,
    val centerRightPercent: Double = 0.0,
    val rightPercent: Double = 0.0,
    val articleCount: Int = 0,
    val newsItems: List<NewsItemDto> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    companion object {
        /**
         * NewsCluster 엔티티를 DTO로 변환
         */
        fun fromEntity(entity: NewsCluster, includeItems: Boolean = true): NewsClusterDto {
            // 날짜 포맷 처리
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            val createdAtString = entity.createdAt.format(formatter)
            val updatedAtString = entity.updatedAt.format(formatter)
            
            return NewsClusterDto(
                id = entity.id,
                topic = entity.topic,
                topicDescription = entity.topicDescription,
                representativeImageUrl = entity.representativeImageUrl,
                keywords = entity.keywords,
                leftPercent = entity.leftPercent,
                centerLeftPercent = entity.centerLeftPercent,
                centerPercent = entity.centerPercent,
                centerRightPercent = entity.centerRightPercent,
                rightPercent = entity.rightPercent,
                articleCount = entity.articleCount,
                newsItems = if (includeItems) entity.newsItems.map { NewsItemDto.fromEntity(it) } else emptyList(),
                createdAt = createdAtString,
                updatedAt = updatedAtString
            )
        }
    }
}
