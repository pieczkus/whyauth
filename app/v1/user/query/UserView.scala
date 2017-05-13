package v1.user.query

import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, Props}
import akka.persistence.query.EventEnvelope
import akka.stream.ActorMaterializer
import com.sksamuel.elastic4s.ElasticDsl.{boolQuery, termQuery}
import com.sksamuel.elastic4s.searches.queries.QueryDefinition
import com.sksamuel.elastic4s.searches.sort.FieldSortDefinition
import org.elasticsearch.search.sort.SortOrder
import pl.why.common.ViewBuilder.InsertAction
import pl.why.common.{CommonActor, ElasticSearchSupport, ReadModelObject, ViewBuilder}
import spray.json.JsonFormat
import v1.user.command.UserEntity
import v1.user.command.UserEntity.Event.UserCreated
import v1.user.query.UserViewBuilder.UserRM

trait UserReadModel {
  def indexRoot = "post"

  def entityType = UserEntity.EntityType
}

object UserViewBuilder {
  val Name = "user-view-builder"

  case class UserRM(email: String, name: String, password: String, key: String, role: String, createdOn: Long, deleted: Boolean)
    extends ReadModelObject {
    def id: String = email
  }

  def props(resumableProjectionManager: ActorRef): Props = Props(new UserViewBuilder(resumableProjectionManager))
}

case class UserViewBuilder @Inject()(@Named("resumable-projection-manager") rpm: ActorRef)
  extends ViewBuilder[UserViewBuilder.UserRM](rpm) with UserReadModel with UserJsonProtocol {

  override implicit val rmFormats: JsonFormat[UserViewBuilder.UserRM] = userRmFormat

  override def projectionId: String = UserViewBuilder.Name

  override def actionFor(id: String, env: EventEnvelope): ViewBuilder.IndexAction = env.event match {
    case UserCreated(u) =>
      val rm = UserRM(u.email, u.name, u.password, u.key, u.role, u.createdOn, u.deleted)
      InsertAction(id, rm)
  }
}

object UserView {
  val Name = "user-view"

  case object FindAllUsers

  def props: Props = Props[UserView]
}

class UserView extends CommonActor with ElasticSearchSupport with UserReadModel with UserJsonProtocol {

  import UserView._
  import context.dispatcher

  implicit val mater = ActorMaterializer()

  lazy val defaultSort = FieldSortDefinition("createdOn", order = SortOrder.DESC)

  lazy val defaultNotDeletedQuery: QueryDefinition = boolQuery().must(termQuery("deleted.keyword", false))


  override def receive: Receive = {
    case FindAllUsers =>
      pipeResponse(queryElasticSearch[UserRM](boolQuery().must(defaultNotDeletedQuery), sort = Some(defaultSort)))
  }
}
