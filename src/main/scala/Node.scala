import Node.{Join, Message}
import akka.actor.{Actor, ActorLogging, Props}

//TODO: findSuccessor naive implementation

class Node extends Actor with ActorLogging {

  private var _nodeName: String = ""

  def nodeName = _nodeName

  def nodeName_=(name: String): Unit = {
    _nodeName = name
  }

  override def receive: Receive = {
    case Message(message) =>
      log.info(this.nodeName)
      log.info(s"Message received (from ${sender()}): $message")
    //    case Confuguration("get") =>
    //      log.info(s"Message received (from ${sender()} to get configuration)")
    //      sender() ! Message("Get your configuration")
    case Join(name) =>
      log.info("Join request")
      if (this.nodeName.isEmpty) {
        this.nodeName = name
      }
      context.actorSelection("/user/*") ! Message(this.nodeName + " joined")

  }
}

object Node {

  case class Message(message: String)

  case class Join(nodeName: String)

  case class Confuguration(message: String)

  def props(): Props = Props(new Node)

}
