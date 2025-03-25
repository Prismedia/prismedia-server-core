package com.prismedia.server.repository

import com.prismedia.server.domain.NewsItem
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NewsItemRepository : JpaRepository<NewsItem, String> {
    
    /**
     * 카테고리로 뉴스 아이템 검색
     */
    fun findByCategory(category: String, pageable: Pageable): Page<NewsItem>
    
    /**
     * 제목에 검색어가 포함된 뉴스 아이템 검색
     */
    fun findByTitleContaining(keyword: String, pageable: Pageable): Page<NewsItem>
    
    /**
     * 카테고리 및 제목으로 뉴스 아이템 검색
     */
    fun findByCategoryAndTitleContaining(category: String, keyword: String, pageable: Pageable): Page<NewsItem>
}
