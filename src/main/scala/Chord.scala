import java.security.MessageDigest

import Node.{AssignSuccessor, FindSuccessor}
import java.lang.Long

import akka.actor.{ActorRef, ActorSystem}

object Chord extends App {

  val system: ActorSystem = ActorSystem("chordSystem")

  println(new Chord().calculateHash("node1", 0))
  println(new Chord().calculateHash("node2", 0))
  println(new Chord().calculateHash("node3", 0))
  val node1Identifier = new Chord().calculateHash("node1", 0); //871105434
  val node1: ActorRef = system.actorOf(Node.props(node3, node1Identifier, node3Identifier), "node1")

  val node2Identifier = new Chord().calculateHash("node2", 0); //390081470
  val node2: ActorRef =
    system.actorOf(Node.props(node1, node2Identifier, node1Identifier), "node2")

  val node3Identifier = new Chord().calculateHash("node3", 0); //257505598
  val node3: ActorRef =
    system.actorOf(Node.props(node2, node3Identifier, node2Identifier), "node3")

  node1 ! AssignSuccessor("/user/node3", node3Identifier)
  Thread.sleep(1000)
  node1 ! FindSuccessor(871105435)
  node1 ! FindSuccessor(390081471)
//  node ! FindSuccessor

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
