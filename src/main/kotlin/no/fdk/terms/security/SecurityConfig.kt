package no.fdk.terms.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jwt.JwtClaimNames.AUD
import org.springframework.security.oauth2.jwt.JwtClaimValidator
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtIssuerValidator
import org.springframework.security.oauth2.jwt.JwtTimestampValidator
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource

@Configuration
open class SecurityConfig(
    @Value("\${application.cors.originPatterns}")
    val corsOriginPatterns: Array<String>
) {
    @Bean
    open fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            cors {
                configurationSource = CorsConfigurationSource {
                    val config = CorsConfiguration()
                    config.allowCredentials = false
                    config.allowedHeaders = listOf("*")
                    config.maxAge = 3600L
                    config.allowedOriginPatterns = corsOriginPatterns.toList()
                    config.allowedMethods = listOf("GET", "POST", "OPTIONS", "DELETE", "PUT")
                    config
                }
            }
            authorizeHttpRequests {
                authorize(HttpMethod.OPTIONS, "/**", permitAll)
                authorize(HttpMethod.GET, "/terms/org/*/version", permitAll)
                authorize(HttpMethod.GET, "/terms/org/*", authenticated)
                authorize(HttpMethod.GET, "/**", permitAll)
                authorize(anyRequest, authenticated)
            }
            oauth2ResourceServer { jwt { } }
        }
        return http.build()
    }

    @Bean
    open fun jwtDecoder(properties: OAuth2ResourceServerProperties): JwtDecoder {
        val jwtDecoder = NimbusJwtDecoder.withJwkSetUri(properties.jwt.jwkSetUri).build()
        jwtDecoder.setJwtValidator(
                DelegatingOAuth2TokenValidator(
                    JwtTimestampValidator(),
                    JwtIssuerValidator(properties.jwt.issuerUri),
                    JwtClaimValidator(AUD) { aud: List<String> -> aud.contains("terms-and-conditions") }
            )
        )
        return jwtDecoder
    }
}
