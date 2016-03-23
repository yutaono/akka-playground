package playground.tpeandfje

import akka.actor.Actor.Receive
import akka.actor.{ActorLogging, Actor, Props, ActorSystem}
import akka.pattern.ask
import akka.routing.{RoundRobinPool, RoundRobinRoutingLogic, RoundRobinGroup}
import akka.util.Timeout
import com.typesafe.config.{ConfigParseOptions, ConfigFactory}
import scala.collection.immutable.IndexedSeq
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

import scala.util.{Failure, Success, Random}

object TPEAndFJETest extends App {

  val deployConfig = ConfigFactory.parseString(
    """
      |tpe-test-dispatcher {
      |  type = Dispatcher
      |  executor = "thread-pool-executor"
      |  thread-pool-executor {
      |    core-pool-size-min = 2
      |    core-pool-size-factor = 2.0
      |    core-pool-size-max = 4
      |  }
      |  throughput = 2
      |}
      |
      |fje-test-dispatcher {
      |  type = Dispatcher
      |  executor = "fork-join-executor"
      |  fork-join-executor {
      |    parallelism-min = 2
      |    parallelism-factor = 2.0
      |    parallelism-max = 4
      |  }
      |  throughput = 2
      |}
      |
      |akka.actor.deployment {
      |  /tpe-test {
      |    dispatcher = tpe-test-dispatcher
      |  }
      |  /fje-test {
      |    dispatcher = fje-test-dispatcher
      |  }
      |}
    """.stripMargin, ConfigParseOptions.defaults())


  val system = ActorSystem("tpe-and-fje-test", deployConfig)

  implicit val dispatcher = system.dispatcher
  implicit val timeout = Timeout(60 seconds)

//  val whimGreeter = system.actorOf(Props[WhimGreeter].withRouter(RoundRobinPool(2)), name = "tpe-test")
  val whimGreeter = system.actorOf(Props[WhimGreeter].withRouter(RoundRobinPool(2)), name = "fje-test")

  val start = System.currentTimeMillis()

  val futures: IndexedSeq[Future[Any]] = (0 until 100) map { i =>
    whimGreeter ? i
  }

  val sleepTime = Await.result(Future.sequence(futures), Duration.Inf).map(_.toString.toInt).sum
  val calcTime = System.currentTimeMillis() - start

//  println(s"sleep time: ${sleepTime} calc time: ${calcTime}, diff: ${calcTime - sleepTime}, efficiency: ${sleepTime.toDouble / calcTime.toDouble}")
  println(s"calc time: ${calcTime}")

  system.terminate()
  sys.exit()
}

class WhimGreeter extends Actor with ActorLogging {

  val r = new Random()

  override def receive: Receive = {
    case num =>
      log.info(s"${num} received.")
      val ms = sleepRandomMilliSeconds((100, 1000))
      log.info(s"${num} received and waited ${ms} ms.")
      sender() ! ms
  }

  private def sleepRandomMilliSeconds(range: (Int, Int)): Int = {
    val ms = range._1 + r.nextInt(range._2 - range._1 + 1 )
//    Thread.sleep(ms)

    for {
      i <- 0 to range._1 + r.nextInt(range._2 - range._1 + 1 )
      j <- 0 to range._1 + r.nextInt(range._2 - range._1 + 1 )
    } yield i + j

    ms
  }
}
