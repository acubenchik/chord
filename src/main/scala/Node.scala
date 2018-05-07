import java.util.concurrent.TimeUnit

import Node._
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
        println("Found a responsible node for key " + id + " in node " + this.nodeHashValue)
      } else {
        this.successor ! FindSuccessor(id: Int)
      }
    case AssignSuccessor(name: String, successorHashValue: Int) =>

      this.successorHashValue = successorHashValue
      val TIMEOUT: Timeout = Timeout(100, TimeUnit.MILLISECONDS)
//      context.actorSelection(name).resolveOne()(TIMEOUT).onComplete(res => {
//        this.successor = res.get
//      })
    case Join(name: String, identifier: Int) =>
      println("Join request with id " + identifier + " received in node " + this.nodeHashValue + ", current node successor is " + this.successorHashValue)
      if ((identifier > nodeHashValue && identifier <= successorHashValue)
        || (identifier > nodeHashValue && successorHashValue - nodeHashValue < 0)) {
        println("Found a predecessor node to join " + this.nodeHashValue)
        val previousSuccessorHash = this.successorHashValue
        val previousSuccessor = this.successor
        val TIMEOUT: Timeout = Timeout(100, TimeUnit.MILLISECONDS)
//        context.actorSelection(name).resolveOne()(TIMEOUT).onComplete(res => {
//          this.successor = res.get // node that wants to join becomes current successor
//          this.successorHashValue = identifier
//          res.get ! Configuration(previousSuccessor, previousSuccessorHash) // send new config to newJoiner???
//        })
      } else {
        println("Join request with id " + identifier + " passed further from node " + this.nodeHashValue)
        this.successor ! Join(name, identifier)
      }
    case Configuration(successor: ActorRef, successorHash: Int) =>
      println("Received configuration for node " + this.nodeHashValue + " new successor is " + successor + " new successorValue is " + successorHash)
      this.successor = successor
      this.successorHashValue = successorHash
//    case Leave(nodeName: String, identifier: Int) =>
//      if(this.nodeHashValue == identifier) {
//        println("Current node will leave now")
//        this.successor = null
//        this.successorHashValue = -1
//      } else {
//        val TIMEOUT: Timeout = Timeout(100, TimeUnit.MILLISECONDS)
//        context.actorSelection(nodeName).resolveOne()(TIMEOUT).onComplete(res => {
//          res.get ! Leave(nodeName, identifier)
//        })
//      }


  }
}

object Node {

  sealed abstract class Command

  case class FindSuccessor(id: Int) extends Command

  case class Leave(nodeName: String, identifier: Int)  extends Command

  case class AssignSuccessor(name: String, successorHashValue: Int) extends Command

  case class Join(name: String, identifier: Int) extends Command

  case class Configuration(successor: ActorRef, successorHash: Int) extends Command

  def props(successor: ActorRef, nodeHashValue: Int, successorHashValue: Int): Props =
    Props(new Node(successor, nodeHashValue, successorHashValue))

}
