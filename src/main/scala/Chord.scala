import java.security.MessageDigest

import Node.{AssignSuccessor, FindSuccessor, Join}
import java.lang.Long

import akka.actor.{ActorRef, ActorSystem}

object Chord extends App {

  val system: ActorSystem = ActorSystem("chordSystem")

  val node1Identifier = 30
//    new Chord().calculateHash("node1", 0); //871105434
  val node1: ActorRef = system.actorOf(Node.props(node3, node1Identifier, node3Identifier), "node1")

  val node2Identifier = 20
//    new Chord().calculateHash("node2", 0); //390081470
  val node2: ActorRef =
    system.actorOf(Node.props(node1, node2Identifier, node1Identifier), "node2")

  val node3Identifier = 10
//    new Chord().calculateHash("node3", 0); //257505598
  val node3: ActorRef =
    system.actorOf(Node.props(node2, node3Identifier, node2Identifier), "node3")

  node1 ! AssignSuccessor("/user/node3", node3Identifier)
  Thread.sleep(1000)
  node1 ! FindSuccessor(19)

  val node4Identifier: Int = 15
  val node4: ActorRef =
    system.actorOf(Node.props(null, node4Identifier, -1), "node4")

  node1 ! Join("/user/node4", node4Identifier)

  Thread.sleep(1000)
  node2 ! FindSuccessor(19)

}

class Chord {
  def calculateHash(nodeName: String, totalLength: Int): Int = {
    val totalSpace: Int = Math.pow(2, 30).toInt
    var key = MessageDigest.getInstance("SHA-1").digest(nodeName.getBytes("UTF-8")).map("%02X" format _).mkString.trim()
    if (key.length > 15)
      key = key.substring(key.length - 15)
    (Long.parseLong(key, 16) % totalSpace).toInt
  }

}
