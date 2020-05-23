package no.fdk.terms.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding


@ConstructorBinding
@ConfigurationProperties("application.secrets")
data class SecurityProperties (
    val userApiKey: String
)