import java.lang.Long
import java.security.MessageDigest

import HistoryActor.NodeJoined
import Node.{Join, RetrieveConfiguration}
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._

import scala.concurrent.Future

object ChordServer extends App {

  implicit val system: ActorSystem = ActorSystem("chordSystem")

  val historyActor: ActorRef = system.actorOf(HistoryActor.props(), "history1")
//    val node1Identifier = 30
//  ////    new Chord().calculateHash("node1", 0); //871105434
//    val node1: ActorRef = system.actorOf(Node.props(node1Identifier), "node1")
//
//    val node2Identifier = 20
//    val node2: ActorRef = system.actorOf(Node.props(node2Identifier), "node2")
//    node1 ! Join(node2, node2Identifier)
//
//    Thread.sleep(1000)
//    val node3Identifier = 15
//    val node3: ActorRef = system.actorOf(Node.props(node3Identifier), "node3")
//    node2 ! Join(node3, node3Identifier)
//    Thread.sleep(1000)
//    node3 ! RetrieveConfiguration
//    node2 ! RetrieveConfiguration
//    node1 ! RetrieveConfiguration

  historyActor ! NodeJoined("testNode1")

//  implicit val materializer = ActorMaterializer()
//
//  implicit val executionContext = system.dispatcher
//  val serverSource: Source[Http.IncomingConnection, Future[Http.ServerBinding]] =
//    Http().bind(interface = "localhost", port = 8080)
//    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")


}

class ChordServer {
  def calculateHash(nodeName: String, totalLength: Int): Int = {
    val totalSpace: Int = Math.pow(2, 30).toInt
    var key = MessageDigest.getInstance("SHA-1").digest(nodeName.getBytes("UTF-8")).map("%02X" format _).mkString.trim()
    if (key.length > 15)
      key = key.substring(key.length - 15)
    (Long.parseLong(key, 16) % totalSpace).toInt
  }

}
