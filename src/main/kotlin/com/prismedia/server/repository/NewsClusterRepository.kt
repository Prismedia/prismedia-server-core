package com.prismedia.server.repository

import com.prismedia.server.domain.NewsCluster
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NewsClusterRepository : JpaRepository<NewsCluster, Long> {
    
    /**
     * 토픽으로 뉴스 클러스터 검색
     */
    fun findByTopicContaining(topic: String, pageable: Pageable): Page<NewsCluster>
    
    /**
     * 키워드로 뉴스 클러스터 검색
     */
    fun findByKeywordsContaining(keywords: String, pageable: Pageable): Page<NewsCluster>
}
