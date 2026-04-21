package no.fdk.terms.repository

import no.fdk.terms.model.TermsAndConditions
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TermsRepository : JpaRepository<TermsAndConditions, String> {
    fun findFirstByOrderByVersionDesc(): TermsAndConditions?
}
