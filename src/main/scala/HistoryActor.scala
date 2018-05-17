import HistoryActor.{NodeJoined, NodeJoinedEvent}
import akka.actor.Props
import akka.persistence._

class HistoryActor extends PersistentActor {
  private var state: HistoryState = new HistoryState

  private def updateState(event: NodeJoinedEvent): Unit = {
    this.state.joinNode()
  }

  override def receiveRecover: Receive = {
    case evt: NodeJoinedEvent =>
      updateState(evt)
      println("Received recover event")
    //    case SnapshotOffer(_, snapshot: ExampleState) ⇒ state = snapshot
  }

  override def receiveCommand: Receive = {
    case NodeJoined(name) => persist(NodeJoinedEvent(name)) {
      event => {
        println("Event persisted")
        updateState(event)
      }
    }
  }

  override def persistenceId: String = "history-actor"
}

class HistoryState {
  private var _numberOfNodes: Int = 0

  def numberOfNodes: Int = _numberOfNodes

  def joinNode(): Unit = {
    _numberOfNodes = _numberOfNodes + 1
    println(" number of node now is " + _numberOfNodes)
  }

}

object HistoryActor {

  sealed trait HistoryEvent

  case class NodeJoinedEvent(name: String) extends HistoryEvent

  sealed trait HistoryCommand

  case class NodeJoined(name: String) extends HistoryCommand

  def props(): Props =
    Props(new HistoryActor())

}