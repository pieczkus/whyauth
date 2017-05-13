package v1.user

import javax.inject.{Inject, Named, Provider}

import akka.actor.ActorRef
import akka.util.Timeout
import pl.why.common.{EmptyResult, FullResult, ServiceResult, SuccessResult}
import play.api.libs.json.{JsValue, Json, Writes}
import v1.user.command.UserData
import v1.user.command.UserManager.AddUser

import scala.concurrent.{ExecutionContext, Future}

case class UserResource(email: String, name: String, password: String, key: String, role: String, createdOn: Long, deleted: Boolean)

object UserResource {
  implicit val implicitWrites = new Writes[UserResource] {
    def writes(u: UserResource): JsValue = {
      Json.obj(
        "email" -> u.email,
        "name" -> u.name,
        "password" -> u.password,
        "key" -> u.key,
        "role" -> u.role,
        "createdOn" -> u.createdOn,
        "deleted" -> u.deleted
      )
    }
  }
}

class UserResourceHandler @Inject()(routerProvider: Provider[UserRouter],
                                    @Named("user-manager") userMnager: ActorRef,
                                    @Named("user-view") userView: ActorRef)
                                   (implicit ec: ExecutionContext) {

  import akka.pattern.ask
  import scala.concurrent.duration._

  implicit val timeout: Timeout = 5.seconds

  def create(input: AddUserInput): Future[ServiceResult[Any]] = {
    (userMnager ? AddUser(input)).mapTo[ServiceResult[UserData]].map {
      case FullResult(_) => SuccessResult
      case _ => EmptyResult
    }
  }

}
