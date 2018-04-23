import javax.security.auth.login.Configuration

import Node.{Confuguration, Join, Message}
import akka.actor.{ActorRef, ActorSystem}

object Chord extends App {

  val m: Int = math.ceil(math.log10(Integer.MAX_VALUE) / math.log10(2)).toInt - 1
  print(m)
  val system: ActorSystem = ActorSystem("chordSystem")

  val masterNode: ActorRef = system.actorOf(Node.props(), "masterNode")

  val node: ActorRef =
    system.actorOf(Node.props(), "node1")

    masterNode ! Join("master")
    node ! Join("node1")

  system.actorSelection("//chordSystem/user/node1")

}
