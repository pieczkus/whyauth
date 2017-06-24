import javax.inject._

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import pl.why.common.resumable.ResumableProjectionManager
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.{Configuration, Environment}
import v1.auth.jwt.{JwtHelper, JwtSecret}
import v1.user.command.UserManager
import v1.user.query.UserView

class Module (environment: Environment, configuration: Configuration)
  extends AbstractModule with ScalaModule with AkkaGuiceSupport {

  override def configure() = {

    bindActor[ResumableProjectionManager](ResumableProjectionManager.Name)

    bindActor[UserManager](UserManager.Name)
    bindActor[UserView](UserView.Name)

    bind(classOf[JwtSecret]).asEagerSingleton()
    bind(classOf[JwtHelper]).asEagerSingleton()
    bind(classOf[ClusterSingleton]).asEagerSingleton()
  }
}
