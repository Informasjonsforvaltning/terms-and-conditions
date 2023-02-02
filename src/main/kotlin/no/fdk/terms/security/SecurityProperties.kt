package no.fdk.terms.security

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties("application.secrets")
data class SecurityProperties (
    val userApiKey: String
)