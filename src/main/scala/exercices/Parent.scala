package exercices

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorRef, ActorSystem, OneForOneStrategy, Props}
import exercices.Child.{RestartException, ResumeException, StopException}
import scala.concurrent.duration._


import scala.concurrent.duration.Duration

class Parent extends Actor {

  var childRef: ActorRef = _

  override def supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 10 seconds) {
    case ResumeException => Resume
    case StopException => Stop
    case RestartException => Restart
    case Exception => Escalate
  }


  override def preStart(): Unit = {
    childRef = context.actorOf(Props[Child], "Child")
  }

  override def receive = {
    case msg =>
      println(s"Parent received ${msg}")
      childRef ! msg
      Thread.sleep(100)
  }
}

class Child extends Actor {


  override def preStart(): Unit = {
    println("Child preStart")
  }

  override def postStop(): Unit = {
    println("Child postStop")
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    println("Child preStart")
    super.preRestart(reason, message)
  }

  override def postRestart(reason: Throwable): Unit = {
    println("Child postRestart")
    super.postRestart(reason)
  }

  override def receive = {
    case "Resume" => throw ResumeException
    case "Stop" => throw StopException
    case "Restart" => throw RestartException
    case _ => throw new Exception
  }
}

object Child {

  case object ResumeException extends Exception

  case object StopException extends Exception

  case object RestartException extends Exception

}

object ParentChild extends App {
  val system = ActorSystem("ParentChild")
  val parent = system.actorOf(Props[Parent], "Parent")

  parent ! "Resume"
  Thread.sleep(1000)
  println()

  system.terminate()
}
