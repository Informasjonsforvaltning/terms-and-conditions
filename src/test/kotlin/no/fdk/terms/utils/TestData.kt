package no.fdk.terms.utils

import no.fdk.terms.model.OrgAcceptation
import no.fdk.terms.model.TermsAndConditions
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap
import java.time.LocalDate

const val LOCAL_SERVER_PORT = 5000
const val API_TEST_PORT = 5050

const val MONGO_USER = "testuser"
const val MONGO_PASSWORD = "testpassword"
const val MONGO_PORT = 27017

val MONGO_ENV_VALUES: Map<String, String> = ImmutableMap.of(
    "MONGO_INITDB_ROOT_USERNAME", MONGO_USER,
    "MONGO_INITDB_ROOT_PASSWORD", MONGO_PASSWORD
)

const val USER_API_KEY = "userapisecret"

val TERMS_0 = TermsAndConditions(
    version = "1.0.0",
    text = "FDK terms and conditions"
)

val TERMS_1 = TermsAndConditions(
    version = "1.0.1",
    text = "FDK terms and conditions 1.0.1"
)

val TERMS_2 = TermsAndConditions(
    version = "1.1.0",
    text = "FDK terms and conditions 1.1.0"
)

val TERMS_3 = TermsAndConditions(
    version = "1.2.3",
    text = "FDK terms and conditions 1.2.3"
)

val TERMS_4 = TermsAndConditions(
    version = "1.2.4",
    text = "FDK terms and conditions 1.2.4"
)

val ACCEPTATION_0 = OrgAcceptation(
    orgId = "123456789",
    acceptedVersion = "1.0.0",
    acceptorName = "First Last",
    acceptDate = LocalDate.now().minusWeeks(2)
)

val ACCEPTATION_1 = OrgAcceptation(
    orgId = "112233445",
    acceptedVersion = "1.2.3",
    acceptorName = "Person Update",
    acceptDate = LocalDate.now().minusDays(1)
)

val ACCEPTATION_2 = OrgAcceptation(
    orgId = "987654321",
    acceptedVersion = "1.0.1",
    acceptorName = "Person Delete",
    acceptDate = LocalDate.now().minusWeeks(1)
)

val ACCEPTATION_3 = OrgAcceptation(
    orgId = "554433221",
    acceptedVersion = "1.2.4",
    acceptorName = "Person Create",
    acceptDate = LocalDate.now()
)

val ACCEPTATION_4 = OrgAcceptation(
    orgId = "111222333",
    acceptedVersion = "1.0.0",
    acceptorName = "Unable To Update",
    acceptDate = LocalDate.now().minusWeeks(1)
)

fun termsDBPopulation(): List<org.bson.Document> =
    listOf(TERMS_0, TERMS_1, TERMS_2, TERMS_3, TERMS_4)
        .map { it.mapDBO() }

fun acceptationDBPopulation(): List<org.bson.Document> =
    listOf(ACCEPTATION_0, ACCEPTATION_1, ACCEPTATION_2, ACCEPTATION_4)
        .map { it.mapDBO() }

private fun TermsAndConditions.mapDBO(): org.bson.Document =
    org.bson.Document()
        .append("_id", version)
        .append("text", text)

private fun OrgAcceptation.mapDBO(): org.bson.Document =
    org.bson.Document()
        .append("_id", orgId)
        .append("acceptedVersion", acceptedVersion)
        .append("acceptorName", acceptorName)
        .append("acceptDate", acceptDate)
