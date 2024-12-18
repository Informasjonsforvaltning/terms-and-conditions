package no.fdk.terms.controller

import no.fdk.terms.model.NewVersionNotHighest
import no.fdk.terms.model.TermsAndConditions
import no.fdk.terms.model.VersionNotThreePartSemantic
import no.fdk.terms.service.TermsService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val logger = LoggerFactory.getLogger(TermsController::class.java)

@CrossOrigin
@RestController
@RequestMapping(value = ["/terms"])
class TermsController(
    private val termsService: TermsService
) {

    @PreAuthorize("@authorizer.hasSysAdminPermission(#jwt)")
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createTermsAndConditions(@AuthenticationPrincipal jwt: Jwt, @RequestBody terms: TermsAndConditions): ResponseEntity<Unit> =
        try {
            logger.info("Create terms and conditions, version ${terms.version}")
            termsService.createTermsAndConditions(terms)
            ResponseEntity<Unit>(HttpStatus.CREATED)
        } catch (exception: VersionNotThreePartSemantic) {
            ResponseEntity<Unit>(HttpStatus.BAD_REQUEST)
        } catch (exception: NewVersionNotHighest) {
            ResponseEntity<Unit>(HttpStatus.BAD_REQUEST)
        }

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllTermsAndConditions(): ResponseEntity<List<TermsAndConditions>> {
        logger.info("Get terms and conditions list")
        return ResponseEntity(termsService.getAllTermsAndConditions(), HttpStatus.OK)
    }

    @GetMapping(value = ["/{version}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getTermsAndConditions(@PathVariable version: String): ResponseEntity<TermsAndConditions> {
        logger.info("Get terms and conditions version $version")
        return termsService.getTermsAndConditions(version)
            ?.let { ResponseEntity(it, HttpStatus.OK) }
            ?: ResponseEntity(HttpStatus.NOT_FOUND)
    }

    @GetMapping(value = ["/latest/version"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getLatestVersion(): ResponseEntity<String> =
        termsService.getTermsAndConditions("latest")
            ?.let { ResponseEntity(it.version, HttpStatus.OK) }
            ?: ResponseEntity(HttpStatus.NOT_FOUND)

}