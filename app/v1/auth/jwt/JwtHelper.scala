package v1.auth.jwt

import javax.inject.Inject

import com.nimbusds.jose.{JWSAlgorithm, JWSHeader}
import com.nimbusds.jwt.{JWTClaimsSet, SignedJWT}
import org.joda.time.DateTime
import v1.user.command.UserData

class JwtHelper @Inject()(jwtSecret: JwtSecret) {

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

}
