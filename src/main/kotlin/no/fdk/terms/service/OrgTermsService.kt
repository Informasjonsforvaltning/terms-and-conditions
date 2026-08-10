package no.fdk.terms.service

import no.fdk.terms.model.OrgAcceptation
import no.fdk.terms.model.OrgAcceptationAlreadyExists
import no.fdk.terms.model.OrgAcceptationNotFound
import no.fdk.terms.model.TermsVersionNotFound
import no.fdk.terms.repository.OrgTermsRepository
import no.fdk.terms.repository.TermsRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val logger = LoggerFactory.getLogger(OrgTermsService::class.java)

@Service
class OrgTermsService(
    private val orgTermsRepository: OrgTermsRepository,
    private val termsRepository: TermsRepository,
) {
    fun getOrgAcceptation(orgId: String): OrgAcceptation? = orgTermsRepository.findById(orgId).orElse(null)

    fun getOrgAcceptations(organizations: List<String>): List<OrgAcceptation> = orgTermsRepository.findAllById(organizations)

    fun createOrgAcceptation(acceptation: OrgAcceptation) {
        if (termsRepository.existsById(acceptation.acceptedVersion)) {
            orgTermsRepository
                .findById(acceptation.orgId)
                .orElse(null)
                ?.run { throw OrgAcceptationAlreadyExists() }
                ?: orgTermsRepository.save(acceptation)
        } else {
            throw TermsVersionNotFound()
        }
    }

    fun updateOrgAcceptation(acceptation: OrgAcceptation) {
        if (termsRepository.existsById(acceptation.acceptedVersion)) {
            if (orgTermsRepository.findById(acceptation.orgId).orElse(null) != null) {
                orgTermsRepository.save(acceptation)
            } else {
                throw OrgAcceptationNotFound()
            }
        } else {
            throw TermsVersionNotFound()
        }
    }

    fun deleteOrgAcceptation(orgId: String) = orgTermsRepository.deleteById(orgId)
}
