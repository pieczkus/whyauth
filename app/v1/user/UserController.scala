package v1.user

import javax.inject.Inject

import pl.why.common.SuccessResult
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class AddUserInput(email: String, name: String, password: String)

class UserController @Inject()(cc: ControllerComponents, handler: UserResourceHandler)(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  private lazy val form: Form[AddUserInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "email" -> email,
        "name" -> nonEmptyText,
        "password" -> nonEmptyText
      )(AddUserInput.apply)(AddUserInput.unapply)
    )
  }

  def create: Action[AnyContent] = Action.async { implicit request =>
    processJsonCreateUser()
  }

  private def processJsonCreateUser[A]()(
    implicit request: Request[A]): Future[Result] = {
    def failure(badForm: Form[AddUserInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: AddUserInput) = {
      handler.create(input).map {
        case SuccessResult => Created
        case _ => BadRequest
      }
    }

    form.bindFromRequest().fold(failure, success)
  }

}
