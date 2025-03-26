package com.prismedia.server.service

import com.prismedia.server.domain.NewsCluster
import com.prismedia.server.dto.NewsClusterDto
import com.prismedia.server.repository.NewsClusterRepository
import com.prismedia.server.repository.NewsItemRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NewsClusterService(
    private val newsClusterRepository: NewsClusterRepository,
    private val newsItemRepository: NewsItemRepository
) {

    /**
     * 모든 뉴스 클러스터를 페이지 형식으로 조회
     */
    @Transactional(readOnly = true)
    fun getAllNewsClusters(pageable: Pageable, includeItems: Boolean = false): Page<NewsClusterDto> {
        return newsClusterRepository.findAll(pageable)
            .map { NewsClusterDto.fromEntity(it, includeItems) }
    }

    /**
     * 특정 ID의 뉴스 클러스터 조회
     */
    @Transactional(readOnly = true)
    fun getNewsClusterById(id: Long, includeItems: Boolean = true): NewsClusterDto? {
        return newsClusterRepository.findById(id)
            .map { NewsClusterDto.fromEntity(it, includeItems) }
            .orElse(null)
    }

    /**
     * 키워드로 뉴스 클러스터 검색
     */
    @Transactional(readOnly = true)
    fun searchNewsClusters(
        topic: String? = null,
        keywords: String? = null,
        pageable: Pageable,
        includeItems: Boolean = false
    ): Page<NewsClusterDto> {
        val clusters = when {
            !topic.isNullOrEmpty() -> {
                newsClusterRepository.findByTopicContaining(topic, pageable)
            }
            !keywords.isNullOrEmpty() -> {
                newsClusterRepository.findByKeywordsContaining(keywords, pageable)
            }
            else -> {
                newsClusterRepository.findAll(pageable)
            }
        }
        return clusters.map { NewsClusterDto.fromEntity(it, includeItems) }
    }

    /**
     * 새 뉴스 클러스터 저장
     */
    @Transactional
    fun saveNewsCluster(newsClusterDto: NewsClusterDto): NewsClusterDto {
        val newsCluster = NewsCluster(
            id = 0L, // AutoIncrement ID
            topic = newsClusterDto.topic,
            topicDescription = newsClusterDto.topicDescription,
            representativeImageUrl = newsClusterDto.representativeImageUrl,
            keywords = newsClusterDto.keywords
        )
        
        val savedNewsCluster = newsClusterRepository.save(newsCluster)
        return NewsClusterDto.fromEntity(savedNewsCluster)
    }

    /**
     * 특정 ID의 뉴스 클러스터 업데이트
     */
    @Transactional
    fun updateNewsCluster(id: Long, newsClusterDto: NewsClusterDto): NewsClusterDto? {
        val existingNewsCluster = newsClusterRepository.findById(id).orElse(null) ?: return null
        
        // 업데이트 로직
        with(existingNewsCluster) {
            topic = newsClusterDto.topic
            topicDescription = newsClusterDto.topicDescription
            representativeImageUrl = newsClusterDto.representativeImageUrl
            keywords = newsClusterDto.keywords
        }
        
        val updatedNewsCluster = newsClusterRepository.save(existingNewsCluster)
        return NewsClusterDto.fromEntity(updatedNewsCluster)
    }

    /**
     * 특정 ID의 뉴스 클러스터 삭제
     */
    @Transactional
    fun deleteNewsCluster(id: Long) {
        newsClusterRepository.deleteById(id)
    }
}
