package no.fdk.terms.repository

import no.fdk.terms.model.OrgAcceptation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface OrgTermsRepository : MongoRepository<OrgAcceptation, String>