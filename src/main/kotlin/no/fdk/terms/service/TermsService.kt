package no.fdk.terms.service

import no.fdk.terms.model.NewVersionNotHighest
import no.fdk.terms.model.TermsAndConditions
import no.fdk.terms.model.VersionNotThreePartSemantic
import no.fdk.terms.repository.TermsRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.regex.Pattern

@Service
class TermsService(
    private val termsRepository: TermsRepository
) {

    fun hasDBConnection(): Boolean =
        try {
            termsRepository.count()
            true
        } catch (e: Exception) {
            false
        }

    fun createTermsAndConditions(terms: TermsAndConditions) {
        if (!terms.version.isThreePartSemanticVersion()) {
            throw VersionNotThreePartSemantic()
        } else {
            val currentVersion = getTermsAndConditions("latest")?.version

            if (terms.version.isHigherSemanticVersion(currentVersion)) {
                termsRepository.save(terms)
            } else throw NewVersionNotHighest()
        }
    }

    fun getAllTermsAndConditions(): List<TermsAndConditions> =
        termsRepository.findAll()

    fun getTermsAndConditions(version: String): TermsAndConditions? =
        if (version == "latest") {
            termsRepository.findFirstByOrderByVersionDesc()
        } else termsRepository.findByIdOrNull(version)

}

private fun String.isThreePartSemanticVersion(): Boolean =
    Pattern.matches("\\d+\\.\\d+\\.\\d+", this)

private fun String.isHigherSemanticVersion(oldVersion: String?): Boolean {
    val new: List<Int> = split(".").map { it.toInt() }
    val old: List<Int>? = oldVersion?.split(".")?.map { it.toInt() }

    return when {
        old == null -> true
        new[0] > old[0] -> true
        new[0] == old[0] && new[1] > old[1] -> true
        new[0] == old[0] && new[1] == old[1] && new[2] > old[2] -> true
        else -> false
    }
}
