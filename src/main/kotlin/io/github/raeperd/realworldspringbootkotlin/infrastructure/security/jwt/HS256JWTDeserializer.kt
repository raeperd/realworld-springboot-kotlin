package io.github.raeperd.realworldspringbootkotlin.infrastructure.security.jwt

import com.nimbusds.jwt.JWTParser
import io.github.raeperd.realworldspringbootkotlin.domain.JWTDeserializationException
import io.github.raeperd.realworldspringbootkotlin.domain.JWTDeserializer
import io.github.raeperd.realworldspringbootkotlin.domain.JWTPayload
import java.text.ParseException

class HS256JWTDeserializer : JWTDeserializer {
    override fun deserialize(jwt: String): JWTPayload {
        try {
            val claimsSet = JWTParser.parse(jwt).jwtClaimsSet
            return JWTPayload(
                claimsSet.subject.toLong(),
                claimsSet.expirationTime.toInstant()
            )
        } catch (exception: ParseException) {
            throw JWTDeserializationException(exception)
        } catch (exception: NumberFormatException) {
            throw JWTDeserializationException(exception)
        }
    }
}