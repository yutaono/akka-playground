package playground.sample

import akka.actor.{Actor, ActorLogging}

class AnActor extends Actor with ActorLogging {

  override def receive: Actor.Receive = {
    case i: Int =>
      log.info(s"received ${i}")
  }

}
