package com.prismedia.server.dto

import com.prismedia.server.domain.NewsItem
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 프론트엔드에 전달할 뉴스 아이템 DTO 클래스
 */
data class NewsItemDto(
    val id: String,
    val title: String,
    val preview: String,
    val imageUrl: String? = null,
    val sourceUrl: String? = null,
    val sourceName: String? = null,
    val category: String? = null,
    val leftPercent: Double,
    val centerPercent: Double,
    val rightPercent: Double,
    val date: String? = null,
    val sourceCount: Int? = null,
    val source: String? = null
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
                leftPercent = entity.leftPercent,
                centerPercent = entity.centerPercent,
                rightPercent = entity.rightPercent,
                date = dateString,
                sourceCount = entity.sourceCount,
                source = entity.source
            )
        }
    }
}
