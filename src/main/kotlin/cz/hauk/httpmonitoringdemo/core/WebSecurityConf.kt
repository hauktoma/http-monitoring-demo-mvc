package cz.hauk.httpmonitoringdemo.core

import arrow.core.Option
import arrow.core.getOrElse
import cz.hauk.httpmonitoringdemo.user.UserDBO
import cz.hauk.httpmonitoringdemo.user.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.token.Sha512DigestUtils
import org.springframework.security.crypto.password.DelegatingPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.AuthenticationConverter
import org.springframework.security.web.authentication.AuthenticationFilter
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.stereotype.Service
import java.util.*
import javax.servlet.http.HttpServletRequest


@Configuration
@EnableWebSecurity
class WebSecurityConf(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : WebSecurityConfigurerAdapter() {

    override fun configure(httpSecurity: HttpSecurity) {
        httpSecurity
            .antMatcher("/api/**")
            // 401 instead of 403 when no auth header provided
            .exceptionHandling().authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .addFilterBefore(
                AuthenticationFilter(
                    AuthenticationManager { token ->
                        Option.fromNullable(token as? ApiKeyAuthenticationToken)
                            .flatMap { Option.fromNullable(userRepository.findByApiKey(passwordEncoder.encode(it.apiKey))) }
                            .map { user -> UserAuthentication(user) }
                            .getOrElse { throw BadCredentialsException("Invalid credentials.") }
                    },
                    ApiKeyAuthenticationConverter()
                ).also {
                    it.successHandler = AuthenticationSuccessHandler { _, _, _ -> }
                },
                BasicAuthenticationFilter::class.java
            )
            .csrf().disable() // no need, API only mode
            .authorizeRequests().anyRequest().authenticated()
            .and()
            .logout().disable()
    }
}

class ApiKeyAuthenticationConverter : AuthenticationConverter {
    override fun convert(
        request: HttpServletRequest
    ): Authentication? = Option
        .fromNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
        .filter { it.isNotBlank() && it.startsWith(API_KEY_PREFIX) }
        .map {
            ApiKeyAuthenticationToken(it.substringAfter(API_KEY_PREFIX))
        }
        .orNull()

    companion object {
        private const val API_KEY_PREFIX = "ApiKey "
    }
}

data class ApiKeyAuthenticationToken(
    val apiKey: String
) : AbstractAuthenticationToken(listOf()) {
    override fun getCredentials(): Any = apiKey
    override fun getPrincipal(): Any? = null
}

@Configuration
class PasswordEncoderConf {

    @Bean
    fun delegatingPasswordEncoder(): PasswordEncoder {
        val defaultEncoder: PasswordEncoder = Sha512Encoder()
        val encoder = DelegatingPasswordEncoder("sha512", mapOf("sha512" to defaultEncoder))
        encoder.setDefaultPasswordEncoderForMatches(defaultEncoder)
        return encoder
    }

    class Sha512Encoder : PasswordEncoder {
        override fun encode(
            rawPassword: CharSequence
        ): String = Sha512DigestUtils.shaHex(rawPassword.toString().toByteArray())

        override fun matches(
            rawPassword: CharSequence, encodedPassword: String
        ): Boolean = encode(rawPassword) == encodedPassword
    }
}

@Service
class AuthenticationProvider {
    fun getAuthentication(): UserAuthentication =
        SecurityContextHolder.getContext().authentication as UserAuthentication
}

data class UserAuthentication(
    private val userDBO: UserDBO
) : Authentication {
    override fun getName(): String = userDBO.username
    override fun getAuthorities(): Collection<GrantedAuthority> = listOf(SimpleGrantedAuthority("USER"))
    override fun getCredentials(): Any? = null
    override fun getDetails(): UserDBO = userDBO
    override fun getPrincipal(): UUID = userDBO.id
    override fun isAuthenticated(): Boolean = true
    override fun setAuthenticated(isAuthenticated: Boolean) =
        throw IllegalAccessException("Cannot change authenticated.")
}