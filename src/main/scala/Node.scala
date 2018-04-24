import java.util.concurrent.TimeUnit

import Node.{AssignSuccessor, Configuration, FindSuccessor, Join}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.util.Timeout


class Node(var successor: ActorRef, nodeHashValue: Int, var successorHashValue: Int) extends Actor with ActorLogging {

  private var _nodeName: String = ""

  def nodeName: String = _nodeName

  def nodeName_=(name: String): Unit = {
    _nodeName = name
  }

  override def receive: Receive = {
    case FindSuccessor(id: Int) =>
      if ((id > nodeHashValue && id <= successorHashValue) || (id > nodeHashValue && successorHashValue - nodeHashValue < 0)) {
        println("FOUND in node " + this.nodeHashValue)
      } else {
        this.successor ! FindSuccessor(id: Int)
      }
    case AssignSuccessor(name: String, successorHashValue: Int) =>
      import scala.concurrent.ExecutionContext.Implicits.global
      this.successorHashValue = successorHashValue
      val TIMEOUT: Timeout = Timeout(100, TimeUnit.MILLISECONDS)
      context.actorSelection(name).resolveOne()(TIMEOUT).onComplete(res => {
        this.successor = res.get
      })
    case Join(name: String, identifier: Int) =>
      if ((identifier > nodeHashValue && identifier <= successorHashValue)
        || (identifier > nodeHashValue && successorHashValue - nodeHashValue < 0)) {
        println("FOUND a predcessor node to join " + this.nodeHashValue)
        import scala.concurrent.ExecutionContext.Implicits.global
        val previousSuccessorHash = this.successorHashValue
        val previousSuccessor = this.successor
        val TIMEOUT: Timeout = Timeout(100, TimeUnit.MILLISECONDS)
        context.actorSelection(name).resolveOne()(TIMEOUT).onComplete(res  => {
          this.successor = res.get // node that wants to join becomes current successor
          this.successorHashValue = identifier
          res.get ! Configuration(previousSuccessor, previousSuccessorHash) // send new config to newJoiner???
        })
      } else {
        this.successor ! Join(name, identifier)
      }
    case Configuration(successor: ActorRef, successorHash: Int) =>
      this.successor = successor
      this.successorHashValue = successorHash


  }
}

object Node {

  case class FindSuccessor(id: Int)

  case class AssignSuccessor(name: String, successorHashValue: Int)

  case class Join(name: String, identifier: Int)

  case class Configuration(successor: ActorRef, successorHash: Int)

  def props(successor: ActorRef, nodeHashValue: Int, successorHashValue: Int): Props =
    Props(new Node(successor, nodeHashValue, successorHashValue))

}
