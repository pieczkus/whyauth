package v1.user.query

import pl.why.common.BaseJsonProtocol
import spray.json.RootJsonFormat
import v1.user.query.UserViewBuilder.UserRM

trait UserJsonProtocol extends BaseJsonProtocol {

  implicit val userRmFormat: RootJsonFormat[UserRM] = jsonFormat7(UserRM.apply)
}
