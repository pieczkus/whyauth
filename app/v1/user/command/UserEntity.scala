package v1.user.command

import java.util.UUID

import akka.actor.Props
import com.trueaccord.scalapb.GeneratedMessage
import pl.why.common._
import pl.why.auth.proto.Auth
import v1.user.command.UserEntity.Command.CreateUser
import v1.user.command.UserEntity.Event.UserCreated

case class UserData(email: String, key: String = UUID.randomUUID().toString, name: String, password: String, role: String = "User",
                    createdOn: Long = System.currentTimeMillis(), deleted: Boolean = false) extends EntityFieldsObject[String, UserData] {
  def assignId(id: String): UserData = this.copy(email = id)

  def id: String = email

  def markDeleted: UserData = this.copy(deleted = true)
}

object UserData {
  lazy val empty = UserData("", "", "", "")
}

object UserEntity {

  val EntityType = "user"

  object Command {

    case class CreateUser(user: UserData) extends EntityCommand {
      def entityId: String = user.email
    }

  }

  object Event {

    trait UserEvent extends EntityEvent {
      override def entityType: String = EntityType
    }

    case class UserCreated(u: UserData) extends UserEvent {
      override def toDataModel: Auth.UserCreated = {

        val user = Auth.User(u.email, u.key, u.name, u.password, u.role, u.createdOn, u.deleted)
        Auth.UserCreated(Some(user))
      }
    }

    object UserCreated extends DataModelReader {
      def fromDataModel: PartialFunction[GeneratedMessage, UserCreated] = {
        case dm: Auth.UserCreated =>
          val u = dm.getUser
          UserCreated(UserData(u.email, u.key, u.name, u.password, u.role, u.createdOn, u.deleted))
      }
    }

  }

  def props: Props = Props[UserEntity]

}

class UserEntity extends PersistentEntity[UserData] {

  override def additionalCommandHandling: Receive = {
    case CreateUser(user) =>
      persist(UserCreated(user)) {
        handleEventAndRespond()
      }
  }

  override def isCreateMessage(cmd: Any): Boolean = cmd match {
    case CreateUser(_) => true
    case _ => false
  }

  override def initialState: UserData = UserData.empty

  override def handleEvent(event: EntityEvent): Unit = event match {
    case UserCreated(user) =>
      state = user
  }
}
