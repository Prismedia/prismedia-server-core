package com.prismedia.server.dto

import com.prismedia.server.domain.NewsItem
import com.prismedia.server.domain.PoliticalBias
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class NewsItemDto(
    val id: Long = 0,
    val title: String,
    val preview: String,
    val imageUrl: String? = null,
    val sourceUrl: String? = null,
    val sourceName: String? = null,
    val category: String? = null,
    val politicalBias: PoliticalBias,
    val date: String? = null,
    val sourceCount: Int? = null,
    val source: String? = null,
    val clusterId: Long? = null
) {
    companion object {
        /**
         * NewsItem 엔티티를 DTO로 변환
         */
        fun fromEntity(entity: NewsItem): NewsItemDto {
            // 날짜 포맷 처리
            val dateString = entity.date?.format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            )
            
            return NewsItemDto(
                id = entity.id,
                title = entity.title,
                preview = entity.preview,
                imageUrl = entity.imageUrl,
                sourceUrl = entity.sourceUrl,
                sourceName = entity.sourceName,
                category = entity.category,
                politicalBias = entity.politicalBias,
                date = dateString,
                sourceCount = entity.sourceCount,
                source = entity.source,
                clusterId = entity.newsCluster?.id
            )
        }
    }
}
