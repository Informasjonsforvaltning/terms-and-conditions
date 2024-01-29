package no.fdk.terms.contract

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.fdk.terms.model.OrgAcceptation
import no.fdk.terms.utils.ACCEPTATION_0
import no.fdk.terms.utils.ACCEPTATION_1
import no.fdk.terms.utils.ACCEPTATION_2
import no.fdk.terms.utils.ACCEPTATION_3
import no.fdk.terms.utils.ACCEPTATION_4
import no.fdk.terms.utils.ApiTestContext
import no.fdk.terms.utils.USER_API_KEY
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


private val mapper = jacksonObjectMapper()

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    properties = ["spring.profiles.active=contract-test"],
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(initializers = [ApiTestContext.Initializer::class])
@Tag("contract")
class OrgTerms : ApiTestContext() {

    @Nested
    internal inner class CreateOrgAcceptation {

        @Test
        fun createNotAllowed() {
            val notLoggedIn = apiAuthorizedRequest("/terms/org", mapper.writeValueAsString(ACCEPTATION_3), null, "POST")
            val rootAccess = apiAuthorizedRequest("/terms/org", mapper.writeValueAsString(ACCEPTATION_3), JwtToken(Access.ROOT).toString(), "POST")
            val readAccess = apiAuthorizedRequest("/terms/org", mapper.writeValueAsString(ACCEPTATION_0), JwtToken(Access.ORG_READ).toString(), "POST")
            val wrongOrg = apiAuthorizedRequest("/terms/org", mapper.writeValueAsString(ACCEPTATION_3.copy(orgId = "998877665")), JwtToken(Access.ORG_WRITE).toString(), "POST")

            assertEquals(HttpStatus.UNAUTHORIZED.value(), notLoggedIn["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), rootAccess["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), readAccess["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), wrongOrg["status"])
        }

        @Test
        fun invalidCreate() {
            val alreadyExists = apiAuthorizedRequest("/terms/org", mapper.writeValueAsString(ACCEPTATION_0), JwtToken(Access.ORG_ADMIN).toString(), "POST")
            val versionDoesNotExist = apiAuthorizedRequest("/terms/org", mapper.writeValueAsString(ACCEPTATION_3.copy(acceptedVersion = "1.1.1")), JwtToken(Access.ORG_ADMIN).toString(), "POST")

            assertEquals(HttpStatus.BAD_REQUEST.value(), alreadyExists["status"])
            assertEquals(HttpStatus.BAD_REQUEST.value(), versionDoesNotExist["status"])
        }

        @Test
        fun ableToGetAfterCreate() {
            val rspCreate = apiAuthorizedRequest("/terms/org", mapper.writeValueAsString(ACCEPTATION_3), JwtToken(Access.ORG_WRITE).toString(), "POST")
            Assumptions.assumeTrue(HttpStatus.CREATED.value() == rspCreate["status"])

            val rspGet = apiAuthorizedRequest("/terms/org/${ACCEPTATION_3.orgId}", null, JwtToken(Access.ORG_WRITE).toString(), "GET")
            Assumptions.assumeTrue(HttpStatus.OK.value() == rspGet["status"])

            val bodyGet: OrgAcceptation = mapper.readValue(rspGet["body"] as String)
            assertEquals(ACCEPTATION_3, bodyGet)
        }

    }

    @Nested
    internal inner class GetOrgAcceptation {

        @Test
        fun unableToGetWhenNotLoggedInAsUserWithOrgAccess() {
            val notLoggedIn = apiAuthorizedRequest("/terms/org/${ACCEPTATION_0.orgId}", null, null, "GET")
            val wrongOrg = apiAuthorizedRequest("/terms/org/${ACCEPTATION_1.orgId}", null, JwtToken(Access.ORG_READ).toString(), "GET")

            assertEquals(HttpStatus.UNAUTHORIZED.value(), notLoggedIn["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), wrongOrg["status"])
        }

        @Test
        fun bothReadAndWriteAccessCanGetAcceptation() {
            val rspRead = apiAuthorizedRequest("/terms/org/${ACCEPTATION_0.orgId}", null, JwtToken(Access.ORG_READ).toString(), "GET")
            val rspWrite = apiAuthorizedRequest("/terms/org/${ACCEPTATION_0.orgId}", null, JwtToken(Access.ORG_WRITE).toString(), "GET")

            Assumptions.assumeTrue(HttpStatus.OK.value() == rspRead["status"])
            Assumptions.assumeTrue(HttpStatus.OK.value() == rspWrite["status"])

            val bodyRead: OrgAcceptation = mapper.readValue(rspRead["body"] as String)
            val bodyWrite: OrgAcceptation = mapper.readValue(rspWrite["body"] as String)

            assertEquals(ACCEPTATION_0, bodyRead)
            assertEquals(ACCEPTATION_0, bodyWrite)
        }

    }

    @Nested
    internal inner class UpdateOrgAcceptation {

        @Test
        fun updateNotAllowed() {
            val notLoggedIn = apiAuthorizedRequest("/terms/org/${ACCEPTATION_1.orgId}", mapper.writeValueAsString(ACCEPTATION_1), null, "PUT")
            val rootAccess = apiAuthorizedRequest("/terms/org/${ACCEPTATION_1.orgId}", mapper.writeValueAsString(ACCEPTATION_1), JwtToken(Access.ROOT).toString(), "PUT")
            val readAccess = apiAuthorizedRequest("/terms/org/${ACCEPTATION_0.orgId}", mapper.writeValueAsString(ACCEPTATION_0.copy(acceptedVersion = "1.2.3")), JwtToken(Access.ORG_READ).toString(), "PUT")
            val wrongOrg = apiAuthorizedRequest("/terms/org/${ACCEPTATION_4.orgId}", mapper.writeValueAsString(ACCEPTATION_4.copy(acceptedVersion = "1.2.3")), JwtToken(Access.ORG_WRITE).toString(), "PUT")

            assertEquals(HttpStatus.UNAUTHORIZED.value(), notLoggedIn["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), rootAccess["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), readAccess["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), wrongOrg["status"])
        }

        @Test
        fun invalidCreate() {
            val doesNotExist = apiAuthorizedRequest("/terms/org/${ACCEPTATION_0.orgId}", mapper.writeValueAsString(ACCEPTATION_0.copy(orgId = "333222111")), JwtToken(Access.ORG_ADMIN).toString(), "PUT")
            val versionDoesNotExist = apiAuthorizedRequest("/terms/org/${ACCEPTATION_1.orgId}", mapper.writeValueAsString(ACCEPTATION_1.copy(acceptedVersion = "1.1.1")), JwtToken(Access.ORG_ADMIN).toString(), "PUT")

            assertEquals(HttpStatus.BAD_REQUEST.value(), doesNotExist["status"])
            assertEquals(HttpStatus.BAD_REQUEST.value(), versionDoesNotExist["status"])
        }

        @Test
        fun ableToGetBeforeAndAfterUpdate() {
            val preUpdate = apiAuthorizedRequest("/terms/org/${ACCEPTATION_1.orgId}", null, JwtToken(Access.ORG_WRITE).toString(), "GET")
            Assumptions.assumeTrue(HttpStatus.OK.value() == preUpdate["status"])
            val bodyPreUpdate: OrgAcceptation = mapper.readValue(preUpdate["body"] as String)
            Assumptions.assumeTrue(ACCEPTATION_1 == bodyPreUpdate)

            val toUpdate = ACCEPTATION_1.copy(acceptedVersion = "1.2.4")
            Assumptions.assumeFalse(ACCEPTATION_1 == toUpdate)

            val rspUpdate = apiAuthorizedRequest("/terms/org/${ACCEPTATION_1.orgId}", mapper.writeValueAsString(toUpdate), JwtToken(Access.ORG_ADMIN).toString(), "PUT")
            Assumptions.assumeTrue(HttpStatus.NO_CONTENT.value() == rspUpdate["status"])

            val postUpdate = apiAuthorizedRequest("/terms/org/${ACCEPTATION_1.orgId}", null, JwtToken(Access.ORG_WRITE).toString(), "GET")
            Assumptions.assumeTrue(HttpStatus.OK.value() == postUpdate["status"])
            val bodyPostUpdate: OrgAcceptation = mapper.readValue(postUpdate["body"] as String)
            assertEquals(toUpdate, bodyPostUpdate)
        }

    }

    @Nested
    internal inner class DeleteOrgAcceptation {

        @Test
        fun onlyRootAccessAllowedToDelete() {
            val notLoggedIn = apiAuthorizedRequest("/terms/org/${ACCEPTATION_2.orgId}", null, null, "DELETE")
            val readAccess = apiAuthorizedRequest("/terms/org/${ACCEPTATION_0.orgId}", null, JwtToken(Access.ORG_READ).toString(), "DELETE")
            val writeAccess = apiAuthorizedRequest("/terms/org/${ACCEPTATION_0.orgId}", null, JwtToken(Access.ORG_WRITE).toString(), "DELETE")

            assertEquals(HttpStatus.UNAUTHORIZED.value(), notLoggedIn["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), readAccess["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), writeAccess["status"])
        }

        @Test
        fun notAbleToGetAfterDelete() {
            val preDelete = apiGet("/terms/org/${ACCEPTATION_2.orgId}/version", mapOf(Pair("X-API-KEY", USER_API_KEY)))
            Assumptions.assumeTrue(HttpStatus.OK.value() == preDelete["status"])
            Assumptions.assumeTrue(ACCEPTATION_2.acceptedVersion == preDelete["body"])

            val rspDelete = apiAuthorizedRequest("/terms/org/${ACCEPTATION_2.orgId}", null, JwtToken(Access.ROOT).toString(), "DELETE")
            Assumptions.assumeTrue(HttpStatus.NO_CONTENT.value() == rspDelete["status"])

            val postDelete = apiGet("/terms/org/${ACCEPTATION_2.orgId}/version", mapOf(Pair("X-API-KEY", USER_API_KEY)))
            Assumptions.assumeTrue(HttpStatus.NOT_FOUND.value() == postDelete["status"])
        }

    }

    @Nested
    internal inner class GetOrgAcceptedVersion {

        @Test
        fun foundVersionEqualsVersionSavedToDB() {
            val response = apiGet("/terms/org/${ACCEPTATION_0.orgId}/version", mapOf(Pair("X-API-KEY", USER_API_KEY)))
            Assumptions.assumeTrue(HttpStatus.OK.value() == response["status"])

            assertEquals(ACCEPTATION_0.acceptedVersion, response["body"])
        }

        @Test
        fun respondWithForbiddenWhenApiKeyIsWrong() {
            val response = apiGet("/terms/org/${ACCEPTATION_0.orgId}/version", mapOf(Pair("X-API-KEY", "wrong-key")))
            assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
        }

    }

}
