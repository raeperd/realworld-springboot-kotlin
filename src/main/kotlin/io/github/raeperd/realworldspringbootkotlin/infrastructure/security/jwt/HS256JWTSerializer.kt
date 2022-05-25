package io.github.raeperd.realworldspringbootkotlin.infrastructure.security.jwt

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.github.raeperd.realworldspringbootkotlin.domain.JWTSerializer
import io.github.raeperd.realworldspringbootkotlin.domain.User
import java.time.Instant.now
import java.util.Date.from

class HS256JWTSerializer(hs256secret: HS256Secret) : JWTSerializer {

    private val signer = MACSigner(hs256secret.secret)
    private val header = JWSHeader(JWSAlgorithm.HS256)

    override fun serialize(user: User): String {
        val jwt = SignedJWT(header, user.JWTClaimsSet())
        jwt.sign(signer)
        return jwt.serialize()
    }

    private fun User.JWTClaimsSet(): JWTClaimsSet {
        return JWTClaimsSet.Builder()
            .subject(id.toString())
            .expirationTime(from(now().plusSeconds(12 * 60 * 60)))
            .build()
    }
}

class HS256Secret(val secret: ByteArray) {
    init {
        if (secret.size != 32) {
            throw IllegalArgumentException("HS256Secret needs 32 bytes but ${secret.size} given")
        }
    }
}