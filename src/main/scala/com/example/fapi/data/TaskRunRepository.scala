package com.example.fapi.data

import akka.actor.{Actor, ActorLogging, Props}

import scala.util.Random

object TaskRunRepository {

  case object GetAll

  case class GetTaskRuns(name: String)
  case class GetTaskRun(id: Int)
  // TODO - insufficient - need start & end data at least
  case class AddTaskRun(name: String)

  case class TaskRunAdded(taskRun: TaskRun)
  case class TaskUnknown(name: String)

  final val Name = "taskRun-repository"

  def props(): Props = Props(new TaskRunRepository())
}

class TaskRunRepository extends Actor with ActorLogging {

  import TaskRunRepository._

  private var taskRuns = List.empty[TaskRun]

  override def receive = {
    case GetAll =>
      log.debug("received GetAll command")
      sender() ! taskRuns
      // TODO - add getting with time constraints
    case GetTaskRuns(name: String) =>
      log.debug(s"received GetTaskRuns $name command")
      sender() ! taskRuns.filter(_.name == name)
    case GetTaskRun(id: Int) =>
      log.debug(s"received GetTaskRun $id command")
      sender() ! taskRuns.filter(_.id == Some(id))
        // TODO - service should control for existence of Task first
    case AddTaskRun(name) =>
      log.info(s"Adding new taskRun with name $name")
      val taskRun = TaskRun(name, id = Some(Random.nextInt(Int.MaxValue)))
      taskRuns = taskRun :: taskRuns
      sender() ! TaskRunAdded(taskRun)
  }
}
