package v1.auth

import javax.inject.{Inject, Named, Provider}

import akka.actor.ActorRef
import akka.util.Timeout
import pl.why.common.{EmptyResult, FullResult, ServiceResult, SuccessResult}
import v1.user.command.UserData
import v1.user.command.UserManager.FindUserByEmail
import com.github.t3hnar.bcrypt._

import scala.concurrent.{ExecutionContext, Future}

class AuthHandler @Inject()(@Named("user-manager") userManager: ActorRef,
                            @Named("user-view") userView: ActorRef)
                           (implicit ec: ExecutionContext) {

  import akka.pattern.ask
  import scala.concurrent.duration._

  implicit val timeout: Timeout = 5.seconds

  def authenticate(credentials: Credentials): Future[Option[UserData]] = {
    (userManager ? FindUserByEmail(credentials.email)).mapTo[ServiceResult[UserData]].map {
      case FullResult(user) if credentials.password.isBcrypted(user.password) => Some(user)
      case _ => None
    }
  }


}
