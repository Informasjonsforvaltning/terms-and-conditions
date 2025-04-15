package no.fdk.terms.utils

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings.getDefaultCodecRegistry
import com.mongodb.client.ClientSession
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClientFactory
import com.mongodb.client.MongoClients.create
import no.fdk.terms.utils.ApiTestContext.Companion.mongoContainer
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.springframework.http.HttpStatus
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

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

fun populateDB(){
    val connectionString = ConnectionString("mongodb://${MONGO_USER}:${MONGO_PASSWORD}@localhost:${mongoContainer.getMappedPort(MONGO_PORT)}/termsAndConditions?authSource=admin&authMechanism=SCRAM-SHA-1")
    val pojoCodecRegistry = CodecRegistries.fromRegistries(getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()))

    val client: MongoClient = create(connectionString)
    val mongoDatabase = client.getDatabase("termsAndConditions").withCodecRegistry(pojoCodecRegistry)

    val termsCollection = mongoDatabase.getCollection("catalogTerms")
    termsCollection.insertMany(termsDBPopulation())

    val orgCollection = mongoDatabase.getCollection("catalogAcceptances")
    orgCollection.insertMany(acceptationDBPopulation())

    client.close()
}
