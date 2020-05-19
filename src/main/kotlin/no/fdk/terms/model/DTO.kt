package no.fdk.terms.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

@Document(collection = "org")
class OrgAcceptation (
    @Id val orgId: String,
    val acceptedVersion: String,
    val acceptorName: String,
    val acceptDate: LocalDate
)

@Document(collection = "terms")
class TermsAndConditions (
    @Id val version: String,
    val text: String
)