package no.fdk.terms.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

@Document(collection = "org")
data class OrgAcceptation (
    @Id val orgId: String,
    val acceptedVersion: String,
    val acceptorName: String,
    @JsonDeserialize(using = LocalDateDeserializer::class)
    @JsonSerialize(using = LocalDateSerializer::class)
    val acceptDate: LocalDate
)

@Document(collection = "terms")
data class TermsAndConditions (
    @Id val version: String,
    val text: String
)
