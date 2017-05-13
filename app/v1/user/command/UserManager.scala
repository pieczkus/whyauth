package v1.user.command

import akka.actor.Props
import akka.util.Timeout
import pl.why.common.PersistentEntity.GetState
import pl.why.common._
import v1.user.AddUserInput
import v1.user.command.UserEntity.Command.CreateUser
import v1.user.command.UserManager.{AddUser, FindUserByEmail}
import com.github.t3hnar.bcrypt._

object UserManager {
  val Name = "user-manager"

  case class FindUserByEmail(email: String)

  case class AddUser(input: AddUserInput)

  val EmailNotUniqueError = ErrorMessage("user.email.nonunique", Some("The user email supplied for a create is not unique"))

  def props: Props = Props[UserManager]
}

class UserManager extends Aggregate[UserData, UserEntity] {

  import akka.pattern.ask
  import scala.concurrent.duration._
  import context.dispatcher

  implicit val timeout = Timeout(5.seconds)

  override def entityProps: Props = UserEntity.props

  override def receive: Receive = {
    case FindUserByEmail(email) =>
      forwardCommand(email, GetState(email))

    case AddUser(input) =>
      val stateFut = (entityShardRegion ? GetState(input.email)).mapTo[ServiceResult[UserData]]
      val caller = sender()
      stateFut onComplete {
        case util.Success(FullResult(_)) =>
          caller ! Failure(FailureType.Validation, UserManager.EmailNotUniqueError)

        case util.Success(EmptyResult) =>

          val user = UserData(input.email, name = input.name, password = input.password.bcrypt)
          entityShardRegion.tell(CreateUser(user), caller)

        case _ =>
          caller ! Failure(FailureType.Service, ServiceResult.UnexpectedFailure)
      }

  }
}
