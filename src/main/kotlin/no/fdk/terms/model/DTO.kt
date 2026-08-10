package no.fdk.terms.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "catalog_acceptances")
data class OrgAcceptation(
    @Id
    @Column(name = "org_id")
    val orgId: String = "",
    @Column(name = "accepted_version", nullable = false)
    val acceptedVersion: String = "",
    @Column(name = "acceptor_name", nullable = false)
    val acceptorName: String = "",
    @JsonDeserialize(using = LocalDateDeserializer::class)
    @JsonSerialize(using = LocalDateSerializer::class)
    @Column(name = "accept_date", nullable = false)
    val acceptDate: LocalDate = LocalDate.now(),
)

@Entity
@Table(name = "catalog_terms")
data class TermsAndConditions(
    @Id
    @Column(name = "version")
    val version: String = "",
    @Column(name = "text", columnDefinition = "text", nullable = false)
    val text: String = "",
)
