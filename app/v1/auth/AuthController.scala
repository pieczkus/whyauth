package v1.auth

import javax.inject.Inject

import play.api.Configuration
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import v1.auth.jwt.JwtHelper

import scala.concurrent.{ExecutionContext, Future}

case class Credentials(email: String, password: String)

class AuthController @Inject()(cc: ControllerComponents, handler: AuthHandler,
                               configuration: Configuration,
                               jwtHelper: JwtHelper)(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  private lazy val form: Form[Credentials] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "email" -> email,
        "password" -> nonEmptyText
      )(Credentials.apply)(Credentials.unapply)
    )
  }

  def authenticate: Action[AnyContent] = Action.async { implicit request =>
    processJsonCredentials()
  }

  private def processJsonCredentials[A]()(
    implicit request: Request[A]): Future[Result] = {
    def failure(badForm: Form[Credentials]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: Credentials) = {
      handler.authenticate(input).map {
        case Some(user) => Ok(jwtHelper.generateToken(user))
        case _ => BadRequest
      }
    }

    form.bindFromRequest().fold(failure, success)
  }


}
