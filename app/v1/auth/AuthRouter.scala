package v1.auth

import javax.inject.Inject

import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class AuthRouter @Inject()(controller: AuthController) extends SimpleRouter {

  override def routes: Routes = {

    case POST(p"/") =>
      controller.authenticate

  }

}
