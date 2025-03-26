package com.prismedia.server.util

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import java.util.*

@Component
class CookieUtils(private val objectMapper: ObjectMapper) {

    fun getCookie(request: HttpServletRequest, name: String): Cookie? {
        val cookies = request.cookies
        
        if (cookies != null) {
            for (cookie in cookies) {
                if (cookie.name == name) {
                    return cookie
                }
            }
        }
        
        return null
    }

    fun addCookie(response: HttpServletResponse, name: String, value: String, maxAge: Int) {
        val cookie = Cookie(name, value)
        cookie.path = "/"
        cookie.isHttpOnly = true
        cookie.maxAge = maxAge
        response.addCookie(cookie)
    }

    fun deleteCookie(request: HttpServletRequest, response: HttpServletResponse, name: String) {
        val cookies = request.cookies
        
        if (cookies != null) {
            for (cookie in cookies) {
                if (cookie.name == name) {
                    cookie.value = ""
                    cookie.path = "/"
                    cookie.maxAge = 0
                    response.addCookie(cookie)
                }
            }
        }
    }

    fun serialize(obj: Any): String {
        return Base64.getEncoder()
            .encodeToString(objectMapper.writeValueAsBytes(obj))
    }

    fun <T> deserialize(cookie: Cookie, cls: Class<T>): T {
        return objectMapper.readValue(
            Base64.getDecoder().decode(cookie.value),
            cls
        )
    }
}
