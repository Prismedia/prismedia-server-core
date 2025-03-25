package com.prismedia.server.service

import com.prismedia.server.domain.NewsItem
import com.prismedia.server.dto.NewsItemDto
import com.prismedia.server.repository.NewsItemRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class NewsItemService(private val newsItemRepository: NewsItemRepository) {

    /**
     * 모든 뉴스 아이템을 페이지 형식으로 조회
     */
    @Transactional(readOnly = true)
    fun getAllNewsItems(pageable: Pageable): Page<NewsItemDto> {
        return newsItemRepository.findAll(pageable)
            .map { NewsItemDto.fromEntity(it) }
    }

    /**
     * 특정 ID의 뉴스 아이템 조회
     */
    @Transactional(readOnly = true)
    fun getNewsItemById(id: String): NewsItemDto? {
        return newsItemRepository.findById(id)
            .map { NewsItemDto.fromEntity(it) }
            .orElse(null)
    }

    /**
     * 카테고리별 뉴스 아이템 조회
     */
    @Transactional(readOnly = true)
    fun getNewsItemsByCategory(category: String, pageable: Pageable): Page<NewsItemDto> {
        return newsItemRepository.findByCategory(category, pageable)
            .map { NewsItemDto.fromEntity(it) }
    }

    /**
     * 검색어로 뉴스 아이템 검색
     */
    @Transactional(readOnly = true)
    fun searchNewsItems(keyword: String?, category: String?, pageable: Pageable): Page<NewsItemDto> {
        return when {
            !keyword.isNullOrEmpty() && !category.isNullOrEmpty() -> {
                newsItemRepository.findByCategoryAndTitleContaining(category, keyword, pageable)
            }
            !keyword.isNullOrEmpty() -> {
                newsItemRepository.findByTitleContaining(keyword, pageable)
            }
            !category.isNullOrEmpty() -> {
                newsItemRepository.findByCategory(category, pageable)
            }
            else -> {
                newsItemRepository.findAll(pageable)
            }
        }.map { NewsItemDto.fromEntity(it) }
    }

    /**
     * 새 뉴스 아이템 저장
     */
    @Transactional
    fun saveNewsItem(newsItemDto: NewsItemDto): NewsItemDto {
        val newsItem = with(newsItemDto) {
            NewsItem(
                id = id.ifEmpty { UUID.randomUUID().toString() },
                title = title,
                preview = preview,
                imageUrl = imageUrl,
                sourceUrl = sourceUrl,
                sourceName = sourceName,
                category = category,
                leftPercent = leftPercent,
                centerPercent = centerPercent,
                rightPercent = rightPercent,
                date = date?.let { parseDate(it) },
                sourceCount = sourceCount,
                source = source
            )
        }
        
        val savedNewsItem = newsItemRepository.save(newsItem)
        return NewsItemDto.fromEntity(savedNewsItem)
    }

    /**
     * 특정 ID의 뉴스 아이템 업데이트
     */
    @Transactional
    fun updateNewsItem(id: String, newsItemDto: NewsItemDto): NewsItemDto? {
        val existingNewsItem = newsItemRepository.findById(id).orElse(null) ?: return null
        
        // 업데이트 로직
        with(existingNewsItem) {
            title = newsItemDto.title
            preview = newsItemDto.preview
            imageUrl = newsItemDto.imageUrl
            sourceUrl = newsItemDto.sourceUrl
            sourceName = newsItemDto.sourceName
            category = newsItemDto.category
            leftPercent = newsItemDto.leftPercent
            centerPercent = newsItemDto.centerPercent
            rightPercent = newsItemDto.rightPercent
            date = newsItemDto.date?.let { parseDate(it) }
            sourceCount = newsItemDto.sourceCount
            source = newsItemDto.source
            updatedAt = LocalDateTime.now()
        }
        
        val updatedNewsItem = newsItemRepository.save(existingNewsItem)
        return NewsItemDto.fromEntity(updatedNewsItem)
    }

    /**
     * 특정 ID의 뉴스 아이템 삭제
     */
    @Transactional
    fun deleteNewsItem(id: String) {
        newsItemRepository.deleteById(id)
    }
    
    /**
     * 문자열 날짜를 LocalDateTime으로 파싱
     */
    private fun parseDate(dateString: String): LocalDateTime? {
        return try {
            LocalDateTime.parse(dateString.replace('Z', ' ').trim())
        } catch (e: Exception) {
            null
        }
    }
}
