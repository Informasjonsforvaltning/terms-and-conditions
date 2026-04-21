package no.fdk.terms.utils

import no.fdk.terms.utils.ApiTestContext.Companion.postgresContainer
import org.flywaydb.core.Flyway
import org.springframework.http.HttpStatus
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.sql.DriverManager

fun apiGet(path: String, headers: Map<String,String>): Map<String,Any> {

    return try {
        val connection = URL("http://localhost:$API_TEST_PORT$path").openConnection() as HttpURLConnection
        headers.forEach { connection.setRequestProperty(it.key, it.value) }
        connection.connect()

        if(isOK(connection.responseCode)) {
            val responseBody = connection.inputStream.bufferedReader().use(BufferedReader::readText)
            mapOf(
                "body"   to responseBody,
                "header" to connection.headerFields.toString(),
                "status" to connection.responseCode)
        } else {
            mapOf(
                "status" to connection.responseCode,
                "header" to " ",
                "body"   to " "
            )
        }
    } catch (e: Exception) {
        mapOf(
            "status" to e.toString(),
            "header" to " ",
            "body"   to " "
        )
    }
}

fun apiAuthorizedRequest(path: String, body: String?, token: String?, method: String): Map<String, Any> {
    val connection  = URL("http://localhost:$API_TEST_PORT$path").openConnection() as HttpURLConnection
    connection.requestMethod = method
    connection.setRequestProperty("Content-type", "application/json")
    connection.setRequestProperty("Accept", "application/json")

    if(!token.isNullOrEmpty()) {
        connection.setRequestProperty("Authorization", "Bearer $token")
    }

    return try {
        connection.doOutput = true
        connection.connect()

        if(body != null) {
            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(body)
            writer.close()
        }

        if(isOK(connection.responseCode)){
            mapOf(
                "body"   to connection.inputStream.bufferedReader().use(BufferedReader :: readText),
                "header" to connection.headerFields.toString(),
                "status" to connection.responseCode
            )
        } else {
            mapOf(
                "status" to connection.responseCode,
                "header" to " ",
                "body" to " "
            )
        }
    } catch (e: Exception) {
        mapOf(
            "status" to e.toString(),
            "header" to " ",
            "body"   to " "
        )
    }
}

private fun isOK(response: Int?): Boolean =
    if(response == null) false
    else HttpStatus.resolve(response)?.is2xxSuccessful == true

fun populateDB() {
    Flyway.configure()
        .dataSource(postgresContainer.jdbcUrl, DB_USER, DB_PASSWORD)
        .load()
        .migrate()

    val conn = DriverManager.getConnection(
        postgresContainer.jdbcUrl,
        DB_USER,
        DB_PASSWORD
    )

    conn.createStatement().execute("DELETE FROM catalog_acceptances")
    conn.createStatement().execute("DELETE FROM catalog_terms")

    conn.prepareStatement("INSERT INTO catalog_terms (version, text) VALUES (?, ?)").use { ps ->
        for (term in listOf(TERMS_0, TERMS_1, TERMS_2, TERMS_3, TERMS_4)) {
            ps.setString(1, term.version)
            ps.setString(2, term.text)
            ps.addBatch()
        }
        ps.executeBatch()
    }

    conn.prepareStatement(
        "INSERT INTO catalog_acceptances (org_id, accepted_version, acceptor_name, accept_date) VALUES (?, ?, ?, ?)"
    ).use { ps ->
        for (acc in listOf(ACCEPTATION_0, ACCEPTATION_1, ACCEPTATION_2, ACCEPTATION_4)) {
            ps.setString(1, acc.orgId)
            ps.setString(2, acc.acceptedVersion)
            ps.setString(3, acc.acceptorName)
            ps.setObject(4, acc.acceptDate)
            ps.addBatch()
        }
        ps.executeBatch()
    }

    conn.close()
}
