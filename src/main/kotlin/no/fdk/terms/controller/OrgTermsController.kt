package no.fdk.terms.controller

import no.fdk.terms.model.OrgAcceptation
import no.fdk.terms.model.OrgAcceptationAlreadyExists
import no.fdk.terms.model.OrgAcceptationNotFound
import no.fdk.terms.model.TermsVersionNotFound
import no.fdk.terms.security.EndpointPermissions
import no.fdk.terms.service.OrgTermsService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

private val logger = LoggerFactory.getLogger(OrgTermsController::class.java)

@CrossOrigin
@RestController
@RequestMapping(value = ["/terms/org"])
class OrgTermsController(
    private val orgTermsService: OrgTermsService,
    private val endpointPermissions: EndpointPermissions
) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createOrgAcceptation(@AuthenticationPrincipal jwt: Jwt, @RequestBody accept: OrgAcceptation): ResponseEntity<Unit> =
        if (endpointPermissions.hasOrgWritePermission(jwt, accept.orgId)) {
            logger.info("Accept terms, version ${accept.acceptedVersion}, for organization with id ${accept.orgId}")
            try {
                orgTermsService.createOrgAcceptation(accept)
                ResponseEntity<Unit>(HttpStatus.CREATED)
            } catch (ex: OrgAcceptationAlreadyExists) {
                ResponseEntity<Unit>(HttpStatus.BAD_REQUEST)
            } catch (ex: TermsVersionNotFound) {
                ResponseEntity<Unit>(HttpStatus.BAD_REQUEST)
            }
        } else ResponseEntity<Unit>(HttpStatus.FORBIDDEN)

    @GetMapping(value = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getOrgAcceptation(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: String): ResponseEntity<OrgAcceptation> =
        if (endpointPermissions.hasOrgReadPermission(jwt, id)) {
            logger.info("Get terms acceptations for organization with id $id")
            orgTermsService.getOrgAcceptation(id)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.NOT_FOUND)
        } else ResponseEntity(HttpStatus.FORBIDDEN)

    @PutMapping(value = ["/{id}"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateOrgAcceptation(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: String, @RequestBody accept: OrgAcceptation): ResponseEntity<Unit> =
        when {
            !endpointPermissions.hasOrgWritePermission(jwt, id) -> ResponseEntity<Unit>(HttpStatus.FORBIDDEN)
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

    @DeleteMapping(value = ["/{id}"])
    fun deleteOrgAcceptation(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: String): ResponseEntity<Unit> =
        if (endpointPermissions.hasAdminPermission(jwt)) {
            logger.info("Delete terms acceptations for organization with id $id")
            orgTermsService.deleteOrgAcceptation(id)
            ResponseEntity(HttpStatus.NO_CONTENT)
        } else ResponseEntity(HttpStatus.FORBIDDEN)

    @GetMapping(value = ["/{id}/version"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getOrgAcceptedVersion(httpServletRequest: HttpServletRequest, @PathVariable id: String): ResponseEntity<String> =
        if (endpointPermissions.isFromFDKCluster(httpServletRequest)) {
            orgTermsService.getOrgAcceptation(id)
                ?.let { ResponseEntity(it.acceptedVersion, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.NOT_FOUND)
        } else ResponseEntity(HttpStatus.FORBIDDEN)

}