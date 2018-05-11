import Node._
import akka.actor.{Actor, ActorIdentity, ActorLogging, ActorRef, Props}


class Node(private var nodeHashValue: Int) extends Actor with ActorLogging {

  private var values = Map()
  private var _successorHashValue: Option[Int] = None
  private var _predecessorHashValue: Option[Int] = None
  private var _predecessor: Option[ActorRef] = None
  private var _successor: Option[ActorRef] = None

  def successorHashValue: Option[Int] = _successorHashValue

  def successorHashValue_=(value: Int): Unit = _successorHashValue = Some(value)

  def predecessorHashValue: Option[Int] = _predecessorHashValue

  def predecessorHashValue_=(value: Int): Unit = _predecessorHashValue = Some(value)

  def predecessor: Option[ActorRef] = _predecessor

  def predecessor_=(value: ActorRef): Unit = _predecessor = Some(value)

  def successor: Option[ActorRef] = _successor

  def successor_=(value: ActorRef): Unit = _successor = Some(value)

  override def receive: Receive = {
    case ActorIdentity(_, Some(ref)) =>
      print("Identity found " + ref)
    case RetrieveConfiguration =>
      println("State for " + self + " is: this._successor " + this._successor + " _successorHashValue " + _successorHashValue +
        " _predecessorHashValue " + _predecessorHashValue + " this._successor " + this._successor + " this.predecessor " + this.predecessor)

    case ChangePredecessor(candidate: ActorRef, candidateHash: Int) =>
      this._predecessor = Some(candidate)
      this._predecessorHashValue = Some(candidateHash)
    case ChangeSuccessor(candidate: ActorRef, candidateHash: Int) =>
      this._successor = Some(candidate)
      this._successorHashValue = Some(candidateHash)
    //    case Leave(candidate: ActorRef, candidateHash: Int) =>


    case Join(candidate: ActorRef, candidateHash: Int) =>
      println("Join request with candidateHash " + candidateHash + " received in node " + this.nodeHashValue)
      this._successorHashValue match {
        case Some(value) if value > candidateHash && (this.nodeHashValue < candidateHash || (this.nodeHashValue > value)) =>
          println("Found a position for node " + candidateHash + " after " + this.nodeHashValue)
          candidate ! ChangeSuccessor(this.successor.get, value)
          candidate ! ChangePredecessor(self, this.nodeHashValue)
          this._successor.get ! ChangePredecessor(candidate, candidateHash)
          this._successor = Some(candidate)
          this._successorHashValue = Some(candidateHash)
        case Some(value) if value > candidateHash && this.nodeHashValue > candidateHash =>
          this.predecessor.get ! Join(candidate, candidateHash)
        case Some(value) if value < candidateHash =>
          this._successor.get ! Join(candidate, candidateHash)
        case None if this.nodeHashValue > candidateHash =>
          println("Node hash is " + this.nodeHashValue)
          println("CandidateHash is " + candidateHash)
          candidate ! ChangePredecessor(self, this.nodeHashValue)
          candidate ! ChangeSuccessor(self, this.nodeHashValue)
          this._predecessor = Some(candidate)
          this._successor = Some(candidate)
          this._predecessorHashValue = Some(candidateHash)
          this._successorHashValue = Some(candidateHash)
        case None if this.nodeHashValue < candidateHash =>
          this._predecessor = Some(candidate)
          this._successor = Some(candidate)
          this._predecessorHashValue = Some(candidateHash)
          this._successorHashValue = Some(candidateHash)
      }
  }
}

object Node {

  sealed abstract class Command

  case class ChangePredecessor(candidate: ActorRef, candidateHash: Int) extends Command

  case class ChangeSuccessor(candidate: ActorRef, candidateHash: Int) extends Command

  case class Join(candidate: ActorRef, candidateHash: Int) extends Command

  case class Leave(candidate: ActorRef, candidateHash: Int) extends Command

  case class StoreValue(key: Int, value: Int) extends Command

  case object RetrieveConfiguration extends Command

  def props(nodeHashValue: Int): Props =
    Props(new Node(nodeHashValue))

}
