package playground

import akka.actor.{ActorLogging, Actor, Props, ActorSystem}
import akka.pattern.ask
import akka.routing.FromConfig
import akka.util.Timeout
import scala.concurrent.duration._

object Main extends App {

  implicit val timeout = Timeout(1 seconds)

  val system = ActorSystem("playground")

  val anActor = system.actorOf(FromConfig.props(Props[AnActor]), "an-actor")

  for (i <- 1 to 20) anActor ? i

}

