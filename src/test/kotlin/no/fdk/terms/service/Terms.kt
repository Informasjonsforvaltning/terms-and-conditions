package no.fdk.terms.service

import no.fdk.terms.model.NewVersionNotHighest
import no.fdk.terms.model.TermsAndConditions
import no.fdk.terms.model.VersionNotThreePartSemantic
import no.fdk.terms.repository.TermsRepository
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@Tag("unit")
class Terms {
    private val termsRepository: TermsRepository = mock()
    private val termsService = TermsService(termsRepository)

    @Nested
    internal inner class Create {

            @Test
            fun throwsExceptionWhenVersionIsInvalid() {
                val versionEmpty = TermsAndConditions(version = "", text = "")
                val versionHasOnePart = TermsAndConditions(version = "123", text = "")
                val versionHasTwoParts = TermsAndConditions(version = "1.23", text = "")
                val versionHasMoreThanThreeParts = TermsAndConditions(version = "1.2.3.4", text = "")
                val versionPartOneContainsNonNumber = TermsAndConditions(version = "1s2.2.3", text = "")
                val versionPartTwoContainsNonNumber = TermsAndConditions(version = "1.-4.3", text = "")
                val versionPartThreeContainsNonNumber = TermsAndConditions(version = "1.2.(3)", text = "")

                assertThrows<VersionNotThreePartSemantic> { termsService.createTermsAndConditions(versionEmpty) }
                assertThrows<VersionNotThreePartSemantic> { termsService.createTermsAndConditions(versionHasOnePart) }
                assertThrows<VersionNotThreePartSemantic> { termsService.createTermsAndConditions(versionHasTwoParts) }
                assertThrows<VersionNotThreePartSemantic> { termsService.createTermsAndConditions(versionHasMoreThanThreeParts) }
                assertThrows<VersionNotThreePartSemantic> { termsService.createTermsAndConditions(versionPartOneContainsNonNumber) }
                assertThrows<VersionNotThreePartSemantic> { termsService.createTermsAndConditions(versionPartTwoContainsNonNumber) }
                assertThrows<VersionNotThreePartSemantic> { termsService.createTermsAndConditions(versionPartThreeContainsNonNumber) }
            }

        @Nested
        internal inner class VersionNotHighest {

            @Test
            fun lowerMajor() {
                val existing = TermsAndConditions("12.1.0", "Some text")
                whenever(termsRepository.findFirstByOrderByVersionDesc()).thenReturn(existing)

                val newTerms = TermsAndConditions(version = "3.2.3", text = "Other text")

                assertThrows<NewVersionNotHighest> { termsService.createTermsAndConditions(newTerms) }
            }

            @Test
            fun lowerMinor() {
                val existing = TermsAndConditions("3.21.1", "Some text")
                whenever(termsRepository.findFirstByOrderByVersionDesc()).thenReturn(existing)

                val newTerms = TermsAndConditions(version = "3.9.1", text = "Other text")

                assertThrows<NewVersionNotHighest> { termsService.createTermsAndConditions(newTerms) }
            }

            @Test
            fun lowerPatch() {
                val existing = TermsAndConditions("3.2.100", "Some text")
                whenever(termsRepository.findFirstByOrderByVersionDesc()).thenReturn(existing)

                val newTerms = TermsAndConditions(version = "3.2.99", text = "Other text")

                assertThrows<NewVersionNotHighest> { termsService.createTermsAndConditions(newTerms) }
            }

            @Test
            fun zerosInFrontOfPartIsIgnored() {
                val existing = TermsAndConditions("0003.2.056", "Some text")
                whenever(termsRepository.findFirstByOrderByVersionDesc()).thenReturn(existing)

                val newTerms = TermsAndConditions(version = "3.0002.55", text = "Other text")

                assertThrows<NewVersionNotHighest> { termsService.createTermsAndConditions(newTerms) }
            }

        }

        @Nested
        internal inner class HigherVersionsIsSaved {

            @Test
            fun higherMajor() {
                val existing = TermsAndConditions("12.1.0", "Some text")
                whenever(termsRepository.findFirstByOrderByVersionDesc()).thenReturn(existing)

                val newTerms = TermsAndConditions(version = "13.2.3", text = "Other text")
                termsService.createTermsAndConditions(newTerms)

                verify(termsRepository, times(1)).save(newTerms)
            }

            @Test
            fun higherMinor() {
                val existing = TermsAndConditions("3.2.1", "Some text")
                whenever(termsRepository.findFirstByOrderByVersionDesc()).thenReturn(existing)

                val newTerms = TermsAndConditions(version = "3.3.1", text = "Other text")
                termsService.createTermsAndConditions(newTerms)

                verify(termsRepository, times(1)).save(newTerms)
            }

            @Test
            fun higherPatch() {
                val existing = TermsAndConditions("3.2.56", "Some text")
                whenever(termsRepository.findFirstByOrderByVersionDesc()).thenReturn(existing)

                val newTerms = TermsAndConditions(version = "3.2.60", text = "Other text")
                termsService.createTermsAndConditions(newTerms)

                verify(termsRepository, times(1)).save(newTerms)
            }

            @Test
            fun zerosInFrontOfPartIsIgnored() {
                val existing = TermsAndConditions("03.00000002.1", "Some text")
                whenever(termsRepository.findFirstByOrderByVersionDesc()).thenReturn(existing)

                val newTerms = TermsAndConditions(version = "3.2.002", text = "Other text")
                termsService.createTermsAndConditions(newTerms)

                verify(termsRepository, times(1)).save(newTerms)
            }

        }

    }

    @Nested
    internal inner class GetOne {

        @Test
        fun findLatestUsesCorrectMethod() {
            termsService.getTermsAndConditions("latest")

            verify(termsRepository, times(1)).findFirstByOrderByVersionDesc()
            verify(termsRepository, times(0)).findById("latest")
        }

        @Test
        fun findByIdUseseCorrectMothod() {
            termsService.getTermsAndConditions("1.0.0")

            verify(termsRepository, times(0)).findFirstByOrderByVersionDesc()
            verify(termsRepository, times(1)).findById("1.0.0")
        }

    }

}
