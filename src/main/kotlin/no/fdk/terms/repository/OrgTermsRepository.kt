package no.fdk.terms.repository

import no.fdk.terms.model.OrgAcceptation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrgTermsRepository : JpaRepository<OrgAcceptation, String>
