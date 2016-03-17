package playground.sample

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.routing.FromConfig
import akka.util.Timeout
import kamon.Kamon

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object Main extends App {

  Kamon.start()

  implicit val timeout = Timeout(1 seconds)

  val system = ActorSystem("playground")

  implicit val dispatcher = system.dispatcher

  val anActor = system.actorOf(FromConfig.props(Props[AnActor]), "an-actor")

  val futures = for (i <- 1 to 20) yield {
    anActor ? i
  }


  Future.sequence(futures) onComplete {
    case Success(s) =>
      system.terminate()
      sys.exit()
    case Failure(f) =>
      system.terminate()
      sys.exit()
  }


}

