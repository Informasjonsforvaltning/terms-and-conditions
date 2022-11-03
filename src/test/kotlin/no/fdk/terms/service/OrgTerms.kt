package no.fdk.terms.service

import no.fdk.terms.model.OrgAcceptation
import no.fdk.terms.model.OrgAcceptationAlreadyExists
import no.fdk.terms.model.OrgAcceptationNotFound
import no.fdk.terms.model.TermsVersionNotFound
import no.fdk.terms.repository.OrgTermsRepository
import no.fdk.terms.repository.TermsRepository
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.util.*

@Tag("unit")
class OrgTerms {
    private val orgTermsRepository: OrgTermsRepository = mock()
    private val termsRepository: TermsRepository = mock()
    private val orgTermsService = OrgTermsService(orgTermsRepository, termsRepository)

    @Nested
    internal inner class Create {

        @Test
        fun throwsExceptionWhenVersionDoesNotExist() {
            whenever(termsRepository.existsById("1.2.3")).thenReturn(false)

            val acceptation = OrgAcceptation(
                orgId = "123456789",
                acceptedVersion = "1.2.3",
                acceptorName = "Firstname Lastname",
                acceptDate = LocalDate.now()
            )

            assertThrows<TermsVersionNotFound> { orgTermsService.createOrgAcceptation(acceptation) }
        }

        @Test
        fun throwsExceptionWhenOrgHasAlreadyAccepted() {
            whenever(termsRepository.existsById("1.2.3")).thenReturn(true)

            val existingAcceptation = OrgAcceptation(
                orgId = "123456789",
                acceptedVersion = "1.0.0",
                acceptorName = "Firstname Lastname",
                acceptDate = LocalDate.now().minusWeeks(1)
            )

            whenever(orgTermsRepository.findById("123456789"))
                .thenReturn(Optional.of(existingAcceptation))

            val acceptation = OrgAcceptation(
                orgId = "123456789",
                acceptedVersion = "1.2.3",
                acceptorName = "Firstname Lastname",
                acceptDate = LocalDate.now()
            )

            assertThrows<OrgAcceptationAlreadyExists> { orgTermsService.createOrgAcceptation(acceptation) }
        }

        @Test
        fun savedWhenTermsExistsAndOrgHasNoEarlierAcceptations() {
            whenever(termsRepository.existsById("1.2.3")).thenReturn(true)
            whenever(orgTermsRepository.findById("123456789"))
                .thenReturn(Optional.empty())

            val acceptation = OrgAcceptation(
                orgId = "123456789",
                acceptedVersion = "1.2.3",
                acceptorName = "Firstname Lastname",
                acceptDate = LocalDate.now()
            )

            orgTermsService.createOrgAcceptation(acceptation)

            verify(orgTermsRepository, times(1)).save(acceptation)
        }

    }

    @Nested
    internal inner class Update {

        @Test
        fun throwsExceptionWhenVersionDoesNotExist() {
            whenever(termsRepository.existsById("1.2.3")).thenReturn(false)

            val acceptation = OrgAcceptation(
                orgId = "123456789",
                acceptedVersion = "1.2.3",
                acceptorName = "Firstname Lastname",
                acceptDate = LocalDate.now()
            )

            assertThrows<TermsVersionNotFound> { orgTermsService.updateOrgAcceptation(acceptation) }
        }

        @Test
        fun throwsExceptionWhenOrgHasNoEarlierAcceptations() {
            whenever(termsRepository.existsById("1.2.3")).thenReturn(true)
            whenever(orgTermsRepository.findById("123456789"))
                .thenReturn(Optional.empty())


            val acceptation = OrgAcceptation(
                orgId = "123456789",
                acceptedVersion = "1.2.3",
                acceptorName = "Firstname Lastname",
                acceptDate = LocalDate.now()
            )

            assertThrows<OrgAcceptationNotFound> { orgTermsService.updateOrgAcceptation(acceptation) }
        }

        @Test
        fun savedWhenTermsExistsAndOrgHasEarlierAcceptations() {
            whenever(termsRepository.existsById("1.2.3")).thenReturn(true)

            val existingAcceptation = OrgAcceptation(
                orgId = "123456789",
                acceptedVersion = "1.0.0",
                acceptorName = "Firstname Lastname",
                acceptDate = LocalDate.now().minusWeeks(1)
            )

            whenever(orgTermsRepository.findById("123456789"))
                .thenReturn(Optional.of(existingAcceptation))

            val acceptation = OrgAcceptation(
                orgId = "123456789",
                acceptedVersion = "1.2.3",
                acceptorName = "Firstname Lastname",
                acceptDate = LocalDate.now()
            )

            orgTermsService.updateOrgAcceptation(acceptation)

            verify(orgTermsRepository, times(1)).save(acceptation)
        }

    }

}
