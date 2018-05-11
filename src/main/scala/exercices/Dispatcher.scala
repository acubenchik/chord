package exercices

import java.time.LocalDateTime
import java.util.concurrent.{Executors, TimeUnit}

import akka.actor.{Actor, ActorSystem, Props}
import akka.util.Timeout
import exercices.TaskActor.{Status, Task, WaitTask}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.{Duration, _}
import akka.pattern._

object TaskActor {

  sealed trait Status

  case object Success extends Status
  case object Failure extends Status

  type Task = () => Status

  class WaitTask(timeToWait: Duration = 5 seconds) extends Task {
    override def apply(): Status = {
      println(s"${LocalDateTime.now()} >> Executing WaitTask...")
      Thread.sleep(timeToWait.toMillis)
      println(s"${LocalDateTime.now()} >> Executed WaitTask...")
      Success
    }
  }

}

class TaskActor extends Actor {
  override def receive: Receive = {
    case task: Task =>
      val result = task()
      sender() ! result
  }
}

class Dispatcher(private val system: ActorSystem) {
  def execute(tasks: List[Task]): List[Status] = {
    implicit val context: ExecutionContext = system.dispatchers.defaultGlobalDispatcher
    val futures = tasks.map(task => {
      val actorRef = system.actorOf(Props[TaskActor])
      implicit val timeout: Timeout = Timeout(1, TimeUnit.MINUTES)
      (actorRef ? task).map(_.asInstanceOf[Status])
    })
    val statuses: List[Status] = Await.result(Future.sequence(futures), Duration.Inf)
    statuses
  }

}

object Dispatcher {
  def apply(): Dispatcher = new Dispatcher(ActorSystem("TaskApp"))
}

object TaskApp extends App {
  val engine = Dispatcher()

  val statuses = engine.execute(List(new WaitTask(5 seconds), new WaitTask(5 seconds), new WaitTask(5 seconds)))
  statuses.foreach(println(_))
}
