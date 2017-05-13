package v1.auth.jwt

import javax.inject.Inject

import com.google.common.base.Strings
import com.nimbusds.jose.{JWSAlgorithm, JWSHeader}
import com.nimbusds.jwt.{JWTClaimsSet, SignedJWT}
import org.joda.time.DateTime
import v1.auth.jwt.JwtHelper.{Invalid, Valid, ValidationResult}
import v1.user.command.UserData

import scala.util.Try
import scala.concurrent.Future

object JwtHelper {

  sealed trait ValidationResult

  case object Valid extends ValidationResult

  case object Invalid extends ValidationResult

}

class JwtHelper @Inject()(jwtSecret: JwtSecret) {

  private final val AUTHORIZATION_TOKEN_VALID_START = "Bearer "

  val defaultExpire = 24

  def generateToken(user: UserData, expiresAt: DateTime = DateTime.now().plusHours(defaultExpire)): String = {

    val claims = new JWTClaimsSet.Builder()
      .issuer("whyauth")
      .subject(user.id.toString)
      .expirationTime(expiresAt.toDate)
      .claim("email", user.email)
      .claim("role", user.role)
      .build()

    val signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims)
    signedJWT.sign(jwtSecret.signer)
    signedJWT.serialize()
  }

  def validateToken(token: String): Future[ValidationResult] = {
    if (Strings.isNullOrEmpty(token) || !token.startsWith(AUTHORIZATION_TOKEN_VALID_START)) {
      Future.successful(Invalid)
    } else {
      Try(SignedJWT.parse(token.replace(AUTHORIZATION_TOKEN_VALID_START, ""))).toOption match {
        case Some(t) =>
          val valid = Try(t.verify(jwtSecret.verifier)).getOrElse(false)
          val expired = isTokenExpired(t)
          if (valid && !expired) {
            Future.successful(Valid)

          } else {
            Future.successful(Invalid)
          }
        case _ => Future.successful(Invalid)
      }
    }
  }

  private def isTokenExpired(token: SignedJWT, now: DateTime = DateTime.now) = {
    val expires = new DateTime(token.getJWTClaimsSet.getExpirationTime)
    now.isAfter(expires)
  }

}
