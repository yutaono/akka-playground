package playground.calculatepi

import akka.actor._
import akka.routing.RoundRobinPool
import akka.pattern.ask
import akka.util.Timeout
import kamon.Kamon

// https://gist.github.com/tuxdna/5170249

sealed trait PiMessage
case object Calculate extends PiMessage
case class Work(start: Int, nrOfElements: Int) extends PiMessage
case class Result(value: Double) extends PiMessage
case class PiApproximation(pi: Double, duration: Double) extends PiMessage

case class CalculationCondition(nrOfWorkers: Int, nrOfElements: Int, nrOfMessages: Int)

class Worker extends Actor {

  override def receive: Actor.Receive = {
    case Work(start, nrOfElements) => sender ! Result(calculationPiFor(start, nrOfElements))
  }

  private def calculationPiFor(start: Int, nrOfElements: Int): Double = {
    (0.0 /: (start until (start + nrOfElements))) { case (acc, i) =>
      acc + 4.0 * (1 - (i % 2) * 2) / (2 * i + 1)
    }
  }

}

class Master(calcCondition: CalculationCondition, listener: ActorRef) extends Actor with ActorLogging {

  var pi: Double = _
  var nrOfResults: Int = _
  val start: Long = System.currentTimeMillis()
  val workerRouter = context.actorOf(Props[Worker].withRouter(new RoundRobinPool(calcCondition.nrOfWorkers)), name = "workerRouter")

  override def receive: Actor.Receive = {
    case Calculate =>
      for (i <- 0 until calcCondition.nrOfMessages)
        workerRouter ! Work(i * calcCondition.nrOfElements, calcCondition.nrOfElements)
    case Result(value) =>
      pi += value
      nrOfResults += 1
      if (nrOfResults == calcCondition.nrOfMessages) {
        listener ! (PiApproximation(pi, duration = System.currentTimeMillis() - start), calcCondition)
      }
  }

}

class Listener extends Actor with ActorLogging {
  override def receive: Actor.Receive = {
    case (PiApproximation(pi, duration), CalculationCondition(nrOfWorkers, nrOfElements, nrOfMessages)) =>
      log.info(s"(workers: ${nrOfWorkers}, elems: ${nrOfElements}, msgs: ${nrOfMessages}) => pi: ${pi}, duration: ${duration}")
  }
}

object Main extends App {

  Kamon.start()

  run(CalculationCondition(nrOfWorkers = 4, nrOfElements = 10000, nrOfMessages = 100))
  run(CalculationCondition(nrOfWorkers = 8, nrOfElements = 10000, nrOfMessages = 100))
  run(CalculationCondition(nrOfWorkers = 16, nrOfElements = 10000, nrOfMessages = 100))
  run(CalculationCondition(nrOfWorkers = 32, nrOfElements = 10000, nrOfMessages = 100))

  def run(calculationCondition: CalculationCondition) = {
    val system = ActorSystem("calculate-pi")
    implicit val dispatcher = system.dispatcher
    val listener = system.actorOf(Props[Listener], name = "listener")
    val master = system.actorOf(Props(new Master(calculationCondition, listener)), name = "master")

    master ! Calculate
  }

}
