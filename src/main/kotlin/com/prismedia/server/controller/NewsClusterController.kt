package com.prismedia.server.controller

import com.prismedia.server.dto.NewsClusterDto
import com.prismedia.server.service.NewsClusterService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/clusters")
@CrossOrigin(origins = ["*"]) // TODO(전역 CORS설정 제외필요함!)
class NewsClusterController(private val newsClusterService: NewsClusterService) {

    /**
     * 모든 뉴스 클러스터 조회 (페이징)
     */
    @GetMapping
    fun getAllNewsClusters(
        @PageableDefault(size = 10, sort = ["updatedAt"]) pageable: Pageable,
        @RequestParam(required = false) topic: String?,
        @RequestParam(required = false) keywords: String?,
        @RequestParam(defaultValue = "false") includeItems: Boolean
    ): ResponseEntity<Page<NewsClusterDto>> {
        val newsClusters = if (topic != null || keywords != null) {
            newsClusterService.searchNewsClusters(topic, keywords, pageable, includeItems)
        } else {
            newsClusterService.getAllNewsClusters(pageable, includeItems)
        }
        return ResponseEntity.ok(newsClusters)
    }

    /**
     * 특정 ID의 뉴스 클러스터 조회
     */
    @GetMapping("/{id}")
    fun getNewsClusterById(
        @PathVariable id: Long,
        @RequestParam(defaultValue = "true") includeItems: Boolean
    ): ResponseEntity<NewsClusterDto> {
        val newsCluster = newsClusterService.getNewsClusterById(id, includeItems)
        return if (newsCluster != null) {
            ResponseEntity.ok(newsCluster)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * 새 뉴스 클러스터 생성
     */
    @PostMapping
    fun createNewsCluster(@RequestBody newsClusterDto: NewsClusterDto): ResponseEntity<NewsClusterDto> {
        val savedNewsCluster = newsClusterService.saveNewsCluster(newsClusterDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(savedNewsCluster)
    }

    /**
     * 뉴스 클러스터 업데이트
     */
    @PutMapping("/{id}")
    fun updateNewsCluster(
        @PathVariable id: Long,
        @RequestBody newsClusterDto: NewsClusterDto
    ): ResponseEntity<NewsClusterDto> {
        val updatedNewsCluster = newsClusterService.updateNewsCluster(id, newsClusterDto)
        return if (updatedNewsCluster != null) {
            ResponseEntity.ok(updatedNewsCluster)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * 뉴스 클러스터 삭제
     */
    @DeleteMapping("/{id}")
    fun deleteNewsCluster(@PathVariable id: Long): ResponseEntity<Unit> {
        newsClusterService.deleteNewsCluster(id)
        return ResponseEntity.noContent().build()
    }
}
