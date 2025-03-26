package com.prismedia.server.dto.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignupRequest(
    @field:NotBlank(message = "이름은 필수 입력값입니다")
    val name: String,
    
    @field:NotBlank(message = "이메일은 필수 입력값입니다")
    @field:Email(message = "유효한 이메일 주소를 입력해주세요")
    val email: String,
    
    @field:NotBlank(message = "비밀번호는 필수 입력값입니다")
    @field:Size(min = 6, message = "비밀번호는 최소 6자 이상이어야 합니다")
    val password: String
)

data class LoginRequest(
    @field:NotBlank(message = "이메일은 필수 입력값입니다")
    @field:Email(message = "유효한 이메일 주소를 입력해주세요")
    val email: String,
    
    @field:NotBlank(message = "비밀번호는 필수 입력값입니다")
    val password: String
)
