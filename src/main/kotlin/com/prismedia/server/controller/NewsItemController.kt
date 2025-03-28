package com.prismedia.server.controller

import com.prismedia.server.dto.NewsItemDto
import com.prismedia.server.service.NewsItemService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/news")
class NewsItemController(private val newsItemService: NewsItemService) {

    /**
     * 모든 뉴스 아이템 조회 (페이징)
     */
    @GetMapping
    fun getAllNewsItems(
        @PageableDefault(size = 10, sort = ["date"]) pageable: Pageable,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) category: String?
    ): ResponseEntity<Page<NewsItemDto>> {
        val newsItems = if (keyword != null || category != null) {
            newsItemService.searchNewsItems(keyword, category, pageable)
        } else {
            newsItemService.getAllNewsItems(pageable)
        }
        return ResponseEntity.ok(newsItems)
    }

    /**
     * 특정 ID의 뉴스 아이템 조회
     */
    @GetMapping("/{id}")
    fun getNewsItemById(@PathVariable id: Long): ResponseEntity<NewsItemDto> {
        val newsItem = newsItemService.getNewsItemById(id)
        return if (newsItem != null) {
            ResponseEntity.ok(newsItem)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * 카테고리별 뉴스 아이템 조회 (페이징)
     */
    @GetMapping("/category/{category}")
    fun getNewsItemsByCategory(
        @PathVariable category: String,
        @PageableDefault(size = 10, sort = ["date"]) pageable: Pageable
    ): ResponseEntity<Page<NewsItemDto>> {
        val newsItems = newsItemService.getNewsItemsByCategory(category, pageable)
        return ResponseEntity.ok(newsItems)
    }

    /**
     * 새 뉴스 아이템 생성
     */
    @PostMapping
    fun createNewsItem(@RequestBody newsItemDto: NewsItemDto): ResponseEntity<NewsItemDto> {
        val savedNewsItem = newsItemService.saveNewsItem(newsItemDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(savedNewsItem)
    }

    /**
     * 뉴스 아이템 업데이트
     */
    @PutMapping("/{id}")
    fun updateNewsItem(
        @PathVariable id: Long,
        @RequestBody newsItemDto: NewsItemDto
    ): ResponseEntity<NewsItemDto> {
        val updatedNewsItem = newsItemService.updateNewsItem(id, newsItemDto)
        return if (updatedNewsItem != null) {
            ResponseEntity.ok(updatedNewsItem)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * 뉴스 아이템 삭제
     */
    @DeleteMapping("/{id}")
    fun deleteNewsItem(@PathVariable id: Long): ResponseEntity<Unit> {
        newsItemService.deleteNewsItem(id)
        return ResponseEntity.noContent().build()
    }
}
