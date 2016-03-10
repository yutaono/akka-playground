package playground

import akka.actor.{ActorLogging, Actor}

class AnActor extends Actor with ActorLogging {

  override def receive: Actor.Receive = {
    case i: Int =>
      log.info(s"received ${i}")

  }

}
