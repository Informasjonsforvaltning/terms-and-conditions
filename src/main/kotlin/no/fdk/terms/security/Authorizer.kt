package no.fdk.terms.security;

import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

private const val ROLE_ROOT_ADMIN = "system:root:admin"
private fun roleOrgAdmin(orgnr: String) = "organization:$orgnr:admin"
private fun roleOrgWrite(orgnr: String) = "organization:$orgnr:write"
private fun roleOrgRead(orgnr: String) = "organization:$orgnr:read"

@Component("authorizer")
class Authorizer(
    private val securityProperties: SecurityProperties
) {

    fun hasOrgReadPermission(jwt: Jwt, orgnr: String): Boolean {
        val authorities: String? = jwt.claims["authorities"] as? String

        return when {
            authorities == null -> false
            authorities.contains(roleOrgAdmin(orgnr)) -> true
            authorities.contains(roleOrgWrite(orgnr)) -> true
            authorities.contains(roleOrgRead(orgnr)) -> true
            else -> false
        }
    }

    fun hasOrgAdminPermission(jwt: Jwt, orgnr: String): Boolean {
        val authorities: String? = jwt.claims["authorities"] as? String

        return when {
            authorities == null -> false
            authorities.contains(roleOrgAdmin(orgnr)) -> true
            else -> false
        }
    }

    fun hasSysAdminPermission(jwt: Jwt): Boolean {
        val authorities: String? = jwt.claims["authorities"] as? String

        return authorities?.contains(ROLE_ROOT_ADMIN) ?: false
    }

    fun isFromFDKCluster(apiKey: String?): Boolean {
        return when {
            apiKey == null -> false
            apiKey.contains(securityProperties.userApiKey) -> true
            else -> false
        }
    }
}
