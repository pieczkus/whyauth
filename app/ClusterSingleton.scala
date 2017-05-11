import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}
import v1.user.query.UserViewBuilder

class ClusterSingleton @Inject()(system: ActorSystem, @Named("resumable-projection-manager") rpm: ActorRef) {

  startSingleton(system, UserViewBuilder.props(rpm), UserViewBuilder.Name)

  private def startSingleton(system: ActorSystem, props: Props, managerName: String, terminationMessage: Any = PoisonPill): ActorRef = {

    system.actorOf(
      ClusterSingletonManager.props(
        singletonProps = props,
        terminationMessage = terminationMessage,
        settings = ClusterSingletonManagerSettings(system)),
      managerName)
  }

}


