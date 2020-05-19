package no.fdk.terms.controller

import no.fdk.terms.service.TermsService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class StatusController (
    private val termsService: TermsService
){

    @GetMapping(value = ["/ping"])
    fun ping(): ResponseEntity<Unit> =
        ResponseEntity(HttpStatus.OK)

    @GetMapping(value = ["/ready"])
    fun ready(): ResponseEntity<Unit> {
        termsService.hasDBConnection()
        return ResponseEntity(HttpStatus.OK)
    }

}