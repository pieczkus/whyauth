package v1.auth

import javax.inject.{Inject, Named, Provider}

import akka.actor.ActorRef
import akka.util.Timeout
import pl.why.common.{EmptyResult, FullResult, ServiceResult, SuccessResult}
import v1.user.command.UserData
import v1.user.command.UserManager.FindUserByEmail
import com.github.t3hnar.bcrypt._
import play.api.libs.json.{JsValue, Json, Writes}
import v1.auth.jwt.JwtHelper

import scala.concurrent.{ExecutionContext, Future}

case class LoggedInUser(email: String, token: String)

object LoggedInUser {
  implicit val implicitWrites = new Writes[LoggedInUser] {
    def writes(u: LoggedInUser): JsValue = {
      Json.obj(
        "email" -> u.email,
        "token" -> u.token
      )
    }
  }
}

class AuthHandler @Inject()(@Named("user-manager") userManager: ActorRef,
                            @Named("user-view") userView: ActorRef, jwtHelper: JwtHelper)
                           (implicit ec: ExecutionContext) {

  import akka.pattern.ask
  import scala.concurrent.duration._

  implicit val timeout: Timeout = 5.seconds

  def authenticate(credentials: Credentials): Future[Option[LoggedInUser]] = {
    (userManager ? FindUserByEmail(credentials.email)).mapTo[ServiceResult[UserData]].map {
      case FullResult(user) =>
        if (credentials.password.isBcrypted(user.password))
          Some(LoggedInUser(user.email, jwtHelper.generateToken(user)))
        else
          None
      case _ => None
    }
  }

  def validateToken(token: String): Future[JwtHelper.ValidationResult] = {
    jwtHelper.validateToken(token)
  }


}
