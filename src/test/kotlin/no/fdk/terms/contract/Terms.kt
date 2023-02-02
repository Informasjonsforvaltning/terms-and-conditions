package no.fdk.terms.contract

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.fdk.terms.model.TermsAndConditions
import no.fdk.terms.utils.ApiTestContext
import no.fdk.terms.utils.TERMS_0
import no.fdk.terms.utils.TERMS_1
import no.fdk.terms.utils.TERMS_2
import no.fdk.terms.utils.TERMS_3
import no.fdk.terms.utils.TERMS_4
import no.fdk.terms.utils.apiAuthorizedRequest
import no.fdk.terms.utils.apiGet
import no.fdk.terms.utils.jwk.Access
import no.fdk.terms.utils.jwk.JwtToken
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import kotlin.test.assertEquals
import kotlin.test.assertTrue


private val mapper = jacksonObjectMapper()

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    properties = ["spring.profiles.active=contract-test"],
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(initializers = [ApiTestContext.Initializer::class])
@Tag("contract")
class Terms : ApiTestContext() {

    @Test
    fun getAllContainsVersionsSavedToDB() {
        val response = apiGet("/terms", emptyMap())
        assertEquals(HttpStatus.OK.value(), response["status"])

        val responseBody: List<TermsAndConditions> = mapper.readValue(response["body"] as String)
        Assumptions.assumeTrue(responseBody.isNotEmpty())

        assertTrue(responseBody.contains(TERMS_0))
        assertTrue(responseBody.contains(TERMS_1))
        assertTrue(responseBody.contains(TERMS_2))
        assertTrue(responseBody.contains(TERMS_3))
        assertTrue(responseBody.contains(TERMS_4))
    }

    @Test
    fun getSpecificVersionEqualsVersionSavedToDB() {
        val response = apiGet("/terms/1.0.0", emptyMap())
        Assumptions.assumeTrue(HttpStatus.OK.value() == response["status"])

        val responseBody: TermsAndConditions = mapper.readValue(response["body"] as String)
        assertEquals(TERMS_0, responseBody)
    }

    @Test
    fun latestContainsHighestVersionBeforeAndAfterCreate() {
        val rspLatest = apiGet("/terms/latest", emptyMap())
        val rspLatestVersion = apiGet("/terms/latest/version", emptyMap())

        Assumptions.assumeTrue(HttpStatus.OK.value() == rspLatest["status"])
        Assumptions.assumeTrue(HttpStatus.OK.value() == rspLatestVersion["status"])

        val rspLatestBody: TermsAndConditions = mapper.readValue(rspLatest["body"] as String)

        assertEquals(TERMS_4, rspLatestBody)
        assertEquals(TERMS_4.version, rspLatestVersion["body"])

        val toCreate = TermsAndConditions(version = "1.2.5", text = "Updated terms and conditions")

        val rspCreate = apiAuthorizedRequest("/terms", mapper.writeValueAsString(toCreate), JwtToken(Access.ROOT).toString(), "POST")
        Assumptions.assumeTrue(HttpStatus.CREATED.value() == rspCreate["status"])

        val latestPostCreate = apiGet("/terms/latest", emptyMap())
        val latestVersionPostCreate = apiGet("/terms/latest/version", emptyMap())

        Assumptions.assumeTrue(HttpStatus.OK.value() == latestPostCreate["status"])
        Assumptions.assumeTrue(HttpStatus.OK.value() == latestVersionPostCreate["status"])

        val latestPostCreateBody: TermsAndConditions = mapper.readValue(latestPostCreate["body"] as String)

        assertEquals(toCreate, latestPostCreateBody)
        assertEquals(toCreate.version, latestVersionPostCreate["body"])
    }

    @Nested
    internal inner class InvalidCreate {

        @Test
        fun createDemandsRootAccess() {
            val toCreate = TermsAndConditions(version = "1.2.3", text = "Will not be created")

            val writeCreate = apiAuthorizedRequest("/terms", mapper.writeValueAsString(toCreate), JwtToken(Access.ORG_WRITE).toString(), "POST")
            assertEquals(HttpStatus.FORBIDDEN.value(), writeCreate["status"])

            val readCreate = apiAuthorizedRequest("/terms", mapper.writeValueAsString(toCreate), JwtToken(Access.ORG_WRITE).toString(), "POST")
            assertEquals(HttpStatus.FORBIDDEN.value(), readCreate["status"])

            val noTokenCreate = apiAuthorizedRequest("/terms", mapper.writeValueAsString(toCreate), null, "POST")
            assertEquals(HttpStatus.UNAUTHORIZED.value(), noTokenCreate["status"])
        }

        @Test
        fun invalidVersions() {
            val versionTooLow = TermsAndConditions(version = "0.99.99", text = "Will not be created")
            val rsp0 = apiAuthorizedRequest("/terms", mapper.writeValueAsString(versionTooLow), JwtToken(Access.ROOT).toString(), "POST")
            assertEquals(HttpStatus.BAD_REQUEST.value(), rsp0["status"])

            val versionContainsLetters = TermsAndConditions(version = "1.b.3", text = "Will not be created")
            val rsp1 = apiAuthorizedRequest("/terms", mapper.writeValueAsString(versionContainsLetters), JwtToken(Access.ROOT).toString(), "POST")
            assertEquals(HttpStatus.BAD_REQUEST.value(), rsp1["status"])

            val versionHasTwoParts = TermsAndConditions(version = "1.3", text = "Will not be created")
            val rsp2 = apiAuthorizedRequest("/terms", mapper.writeValueAsString(versionHasTwoParts), JwtToken(Access.ROOT).toString(), "POST")
            assertEquals(HttpStatus.BAD_REQUEST.value(), rsp2["status"])

            val versionHasHasParts = TermsAndConditions(version = "1.2.3.4", text = "Will not be created")
            val rsp3 = apiAuthorizedRequest("/terms", mapper.writeValueAsString(versionHasHasParts), JwtToken(Access.ROOT).toString(), "POST")
            assertEquals(HttpStatus.BAD_REQUEST.value(), rsp3["status"])
        }

    }

}
