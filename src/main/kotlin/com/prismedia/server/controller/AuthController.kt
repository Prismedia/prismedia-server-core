package com.prismedia.server.controller

import com.prismedia.server.dto.auth.SignUpRequest
import com.prismedia.server.dto.auth.SignUpResponse
import com.prismedia.server.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val userService: UserService) {

    /**
     * 회원가입 API
     * 새 사용자 계정을 생성합니다.
     */
    @PostMapping("/signup")
    fun registerUser(@Valid @RequestBody signUpRequest: SignUpRequest): ResponseEntity<SignUpResponse> {
        val signUpResponse = userService.registerUser(signUpRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(signUpResponse)
    }
}
