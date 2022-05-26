package io.github.raeperd.realworldspringbootkotlin.infrastructure.security.jwt

import com.nimbusds.jwt.JWTParser
import io.github.raeperd.realworldspringbootkotlin.domain.User
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.security.SecureRandom
import java.time.Instant.now

@ExtendWith(MockitoExtension::class)
internal class HS256JWTSerializerTest {

    private val secret: HS256Secret

    init {
        val secretBytes = ByteArray(32)
        SecureRandom().nextBytes(secretBytes)
        secret = HS256Secret(secretBytes)
    }

    private val serializer = HS256JWTSerializer(secret)

    @Test
    fun `when HS256Secret with invalid bytes expect throw IllegalArgumentException`() {
        assertThatThrownBy {
            HS256Secret(ByteArray(31))
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `when serialize jwt expect sub and exp jwt claims`(@Mock user: User) {
        whenever(user.id).thenReturn(1)

        val parsedClaimsSet = serializer.serialize(user)
            .let { JWTParser.parse(it) }
            .jwtClaimsSet

        assertThat(parsedClaimsSet.subject).isEqualTo("1")
        assertThat(parsedClaimsSet.expirationTime).isAfter(now().plusSeconds(11 * 60 * 60))
    }
}