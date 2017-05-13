package v1.auth.jwt

import javax.inject.Inject

import com.nimbusds.jose.crypto.{MACSigner, MACVerifier}
import play.api.Configuration

case class JwtSecret @Inject()(configuration: Configuration) {

  private val secret = configuration.underlying.getString("play.http.secret.key")

  val signer = new MACSigner(secret)
  val verifier = new MACVerifier(secret)
}
