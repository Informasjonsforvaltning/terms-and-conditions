package no.fdk.terms.security

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jwt.JwtClaimNames.AUD
import org.springframework.security.oauth2.jwt.JwtClaimValidator
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtIssuerValidator
import org.springframework.security.oauth2.jwt.JwtTimestampValidator
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
open class SecurityConfig {
    @Bean
    open fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeHttpRequests { authorize ->
                authorize.requestMatchers(HttpMethod.OPTIONS).permitAll()
                    .requestMatchers(HttpMethod.GET, "/terms/org/*/version").permitAll()
                    .requestMatchers(HttpMethod.GET, "/terms/org/*").authenticated()
                    .requestMatchers(HttpMethod.GET).permitAll()
                    .anyRequest().authenticated() }
            .oauth2ResourceServer { resourceServer -> resourceServer.jwt() }
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
