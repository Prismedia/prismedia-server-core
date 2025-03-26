package com.prismedia.server.security.oauth2.service

import com.prismedia.server.domain.user.AuthProvider
import com.prismedia.server.domain.user.User
import com.prismedia.server.exception.OAuth2AuthenticationProcessingException
import com.prismedia.server.repository.UserRepository
import com.prismedia.server.security.oauth2.userinfo.OAuth2UserInfo
import com.prismedia.server.security.oauth2.userinfo.OAuth2UserInfoFactory
import com.prismedia.server.security.userdetails.UserPrincipal
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils

@Service
class CustomOAuth2UserService(
    private val userRepository: UserRepository
) : DefaultOAuth2UserService() {

    override fun loadUser(oAuth2UserRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(oAuth2UserRequest)

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User)
        } catch (ex: AuthenticationException) {
            throw ex
        } catch (ex: Exception) {
            throw InternalAuthenticationServiceException(ex.message, ex)
        }
    }

    private fun processOAuth2User(
        oAuth2UserRequest: OAuth2UserRequest,
        oAuth2User: OAuth2User
    ): OAuth2User {
        val registrationId = oAuth2UserRequest.clientRegistration.registrationId
        val oauth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.attributes)

        if (!StringUtils.hasText(oauth2UserInfo.getEmail())) {
            throw OAuth2AuthenticationProcessingException("OAuth2 공급자로부터 이메일을 찾을 수 없습니다")
        }

        val userOptional = userRepository.findByEmail(oauth2UserInfo.getEmail()!!)
        var user: User
        
        if (userOptional.isPresent) {
            user = userOptional.get()
            
            if (user.provider != AuthProvider.valueOf(registrationId.uppercase())) {
                throw OAuth2AuthenticationProcessingException(
                    "이미 ${user.provider} 계정으로 가입되어 있습니다. " +
                            "${user.provider} 계정으로 로그인해 주세요."
                )
            }
            
            user = updateExistingUser(user, oauth2UserInfo)
        } else {
            user = registerNewUser(oAuth2UserRequest, oauth2UserInfo)
        }

        return UserPrincipal.create(user, oAuth2User.attributes)
    }

    private fun registerNewUser(
        oAuth2UserRequest: OAuth2UserRequest,
        oauth2UserInfo: OAuth2UserInfo
    ): User {
        val user = User(
            provider = AuthProvider.valueOf(oAuth2UserRequest.clientRegistration.registrationId.uppercase()),
            providerId = oauth2UserInfo.getId(),
            name = oauth2UserInfo.getName() ?: "이름 없음",
            email = oauth2UserInfo.getEmail()!!,
            imageUrl = oauth2UserInfo.getImageUrl(),
            emailVerified = true
        )

        return userRepository.save(user)
    }

    private fun updateExistingUser(existingUser: User, oauth2UserInfo: OAuth2UserInfo): User {
        existingUser.name = oauth2UserInfo.getName() ?: existingUser.name
        existingUser.imageUrl = oauth2UserInfo.getImageUrl()
        
        return userRepository.save(existingUser)
    }
}
