package no.fdk.terms.controller

import no.fdk.terms.model.OrgAcceptation
import no.fdk.terms.model.OrgAcceptationAlreadyExists
import no.fdk.terms.model.OrgAcceptationNotFound
import no.fdk.terms.model.TermsVersionNotFound
import no.fdk.terms.service.OrgTermsService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val logger = LoggerFactory.getLogger(OrgTermsController::class.java)

@CrossOrigin
@RestController
@RequestMapping(value = ["/terms/org"])
class OrgTermsController(
    private val orgTermsService: OrgTermsService
) {

    @PreAuthorize("@authorizer.hasOrgAdminPermission(#jwt, #accept.orgId)")
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createOrgAcceptation(@AuthenticationPrincipal jwt: Jwt, @RequestBody accept: OrgAcceptation): ResponseEntity<Unit> =
        try {
            logger.info("Accept terms, version ${accept.acceptedVersion}, for organization with id ${accept.orgId}")
            orgTermsService.createOrgAcceptation(accept)
            ResponseEntity<Unit>(HttpStatus.CREATED)
        } catch (ex: OrgAcceptationAlreadyExists) {
            ResponseEntity<Unit>(HttpStatus.BAD_REQUEST)
        } catch (ex: TermsVersionNotFound) {
            ResponseEntity<Unit>(HttpStatus.BAD_REQUEST)
        }

    @PreAuthorize("@authorizer.hasOrgReadPermission(#jwt, #id)")
    @GetMapping(value = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getOrgAcceptation(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: String): ResponseEntity<OrgAcceptation> {
        logger.info("Get terms acceptations for organization with id $id")
        return orgTermsService.getOrgAcceptation(id)
            ?.let { ResponseEntity(it, HttpStatus.OK) }
            ?: ResponseEntity(HttpStatus.NOT_FOUND)
    }

    @PreAuthorize("@authorizer.hasOrgAdminPermission(#jwt, #id)")
    @PutMapping(value = ["/{id}"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateOrgAcceptation(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: String, @RequestBody accept: OrgAcceptation): ResponseEntity<Unit> =
        when {
            id != accept.orgId -> ResponseEntity<Unit>(HttpStatus.BAD_REQUEST)
            else -> {
                logger.info("Accept terms, version ${accept.acceptedVersion}, for organization with id $id")
                try {
                    orgTermsService.updateOrgAcceptation(accept)
                    ResponseEntity<Unit>(HttpStatus.NO_CONTENT)
                } catch (ex: OrgAcceptationNotFound) {
                    ResponseEntity<Unit>(HttpStatus.BAD_REQUEST)
                } catch (ex: TermsVersionNotFound) {
                    ResponseEntity<Unit>(HttpStatus.BAD_REQUEST)
                }
            }
        }

    @PreAuthorize("@authorizer.hasSysAdminPermission(#jwt)")
    @DeleteMapping(value = ["/{id}"])
    fun deleteOrgAcceptation(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: String): ResponseEntity<Unit> {
        logger.info("Delete terms acceptations for organization with id $id")
        orgTermsService.deleteOrgAcceptation(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @PreAuthorize("@authorizer.isFromFDKCluster(#header)")
    @GetMapping(value = ["/{id}/version"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getOrgAcceptedVersion(@RequestHeader("X-API-KEY") header: String, @PathVariable id: String): ResponseEntity<String> =
        orgTermsService.getOrgAcceptation(id)
            ?.let { ResponseEntity(it.acceptedVersion, HttpStatus.OK) }
            ?: ResponseEntity(HttpStatus.NOT_FOUND)

}
