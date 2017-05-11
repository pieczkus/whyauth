package v1.user

import javax.inject.Inject

import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class UserRouter @Inject()(controller: UserController) extends SimpleRouter {
  val prefix = "/v1/devices"

  override def routes: Routes = {

    case POST(p"/") =>
      controller.create

  }
}
