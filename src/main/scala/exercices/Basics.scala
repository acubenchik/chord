package exercices

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorRef, ActorSystem, Kill, OneForOneStrategy, PoisonPill, Props, Terminated}
import exercices.Parent._

import scala.concurrent.duration._

class Basics {

}

object Parent {
  def props(name: String): Props = Props(new Parent(name))

  sealed trait Message

  case object RestartChild extends Message

  case object KillTheChild extends Message

  case object PoisonTheChild extends Message

  case object MakeMeASandwich extends Message

  case object SudoMakeMeASandwich extends Message

  case object Sandwich extends Message

}

class Parent(private var name: String, private val age: Int = 50) extends Actor {
  private var kid: ActorRef = _

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: ArithmeticException ⇒ Stop
      case _: NullPointerException ⇒ Stop
      case _: IllegalArgumentException ⇒ Stop
      case _: Exception ⇒ Escalate
    }

  override def receive = {
    case RestartChild =>
      kid ! RestartChild
    case SudoMakeMeASandwich =>
      kid ! SudoMakeMeASandwich
      kid ! MakeMeASandwich
    case MakeMeASandwich =>
      kid ! MakeMeASandwich
    case Sandwich =>
      println("A Sandwich received")
    case KillTheChild =>
      println("KillChild received by Parent")
      kid ! Kill
    case PoisonTheChild =>
      kid ! PoisonPill
    case Terminated(child) =>
      println("Child terminated " + child)
    case _ => println("Message received by Parent")
  }

  override def preStart(): Unit = {
    println("Parent preStart")
    this.kid = context.actorOf(Child.props("James"))
    context.watch(kid)
  }

  //Override not to call preStart and not to create new child each time
  override def postRestart(reason: Throwable): Unit = {
    println("Parent postRestart")
  }
}

class Child(private var name: String) extends Actor {
  override def receive = {
    case RestartChild => throw new Exception("I don't want!")
    case MakeMeASandwich => throw new Exception("I dont want")
    case SudoMakeMeASandwich => context.become(goodBoy)
    case _ => println("Message received by Child " + _)
  }

  def goodBoy: Receive = {
    case MakeMeASandwich =>
      sender() ! Sandwich
      1 / 0
  }


  override def preStart(): Unit = {
    println("Child preStart")
  }


  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    println("Child preRestart " + reason)
    println("Child was " + this)
    super.preRestart(reason, message)
  }

  override def postRestart(reason: Throwable): Unit = {
    println("Child postRestart " + reason)
    println("Child became " + this)
    super.postRestart(reason)
  }

  override def postStop(): Unit = {
    println("Child poststop")
  }
}


object Child {
  def props(name: String): Props = Props(new Child(name))
}

object Basics extends App {
  val system = ActorSystem("ParentChild")
  val parent = system.actorOf(Parent.props("Alex"), "Parent")
  parent ! "Message"
  //  parent ! KillTheChild
  //  parent ! PoisonTheChild
  //  parent ! RestartChild
  parent ! SudoMakeMeASandwich

}
