package no.fdk.terms

import no.fdk.terms.security.SecurityProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.SpringApplication

@SpringBootApplication
@EnableConfigurationProperties(SecurityProperties::class)
open class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
