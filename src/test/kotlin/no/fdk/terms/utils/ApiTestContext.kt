package no.fdk.terms.utils

import org.slf4j.LoggerFactory
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.postgresql.PostgreSQLContainer
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI

abstract class ApiTestContext {
    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            TestPropertyValues
                .of(
                    "spring.datasource.url=${postgresContainer.jdbcUrl}",
                    "spring.datasource.username=${postgresContainer.username}",
                    "spring.datasource.password=${postgresContainer.password}",
                ).applyTo(configurableApplicationContext.environment)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ApiTestContext::class.java)
        val postgresContainer: PostgreSQLContainer

        init {

            startMockServer()

            postgresContainer =
                PostgreSQLContainer("postgres:16")
                    .withDatabaseName("terms_and_conditions")
                    .withUsername(DB_USER)
                    .withPassword(DB_PASSWORD)

            postgresContainer.start()

            populateDB()

            try {
                val con = URI("http://localhost:6000/ping").toURL().openConnection() as HttpURLConnection
                con.connect()
                if (con.responseCode != 200) {
                    logger.debug("Ping to mock server failed")
                    stopMockServer()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                stopMockServer()
            } catch (e: InterruptedException) {
                e.printStackTrace()
                stopMockServer()
            }
        }
    }
}
