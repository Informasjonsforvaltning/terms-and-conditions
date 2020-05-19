package no.fdk.terms.repository

import no.fdk.terms.model.TermsAndConditions
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface TermsRepository : MongoRepository<TermsAndConditions, String> {
    fun findFirstByOrderByVersionDesc(): TermsAndConditions?
}