/*
 * Copyright 2016 ksilin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.fapi.data

import akka.actor.{ Actor, ActorLogging, Props }
import org.joda.time.DateTime

import scala.util.Random

object TaskRunRepository {

  case object GetAll

  case object GetPending

  case object GetRunning

  case object GetFinished

  case object GetFinishedSuccessfully

  case object GetFailed

  case class GetTaskRuns(name: String)

  case class GetTaskRun(id: String)

  case class AddTaskRun(name: String)

  case class TaskRunAdded(taskRun: TaskRun)

  case class TaskUnknown(name: String)

  case class TaskRunStart(id: String)

  case class Delete(id: String)

  case class Deleted(id: String)

  case class TaskRunSuccess(id: String, message: Option[String] = None)

  case class TaskRunFailure(id: String, message: Option[String] = None)

  case class TaskRunUpdated(id: String)

  case class RunNotPending(id: String)

  case class RunNotRunning(id: String)

  final val Name = "taskRun-repository"

  def props(): Props = Props(new TaskRunRepository())
}

class TaskRunRepository extends Actor with ActorLogging {

  import TaskRunRepository._

  // TODO - superbad - no internal state here, thats what db is for
  private var taskRuns = List.empty[TaskRun]

  override def receive = {
    case GetAll =>
      log.debug("received GetAll command")
      sender() ! taskRuns
    // TODO - add getting with time constraints
    case GetTaskRuns(name: String) =>
      log.debug(s"received GetTaskRuns $name command")
      sender() ! taskRuns.filter(_.name == name)
    case GetTaskRun(name: String) =>
      log.debug(s"received GetTaskRun $name command")
      sender() ! taskRuns.filter(_.name == name)
    case AddTaskRun(name) =>
      log.info(s"Adding new taskRun with name $name")
      val taskRun = TaskRun(name)
      taskRuns = taskRun :: taskRuns
      sender() ! TaskRunAdded(taskRun)

    case GetPending              => sender() ! pending()
    case GetRunning              => sender() ! running()
    case GetFinished             => sender() ! finished()
    case GetFinishedSuccessfully => sender() ! finished().filter(_.successful)
    case GetFailed               => sender() ! finished().filterNot(_.successful)

    case Delete(name) =>
      taskRuns = taskRuns.filterNot(_.name == name)
      sender() ! Deleted(name)

    case TaskRunStart(name) =>
      pending().filter(_.name == name) match {
        case Nil => sender() ! RunNotPending(name)
        case head :: Nil =>
          taskRuns = head.copy(startedAt = Some(DateTime.now)) :: taskRuns.filterNot(_.name == name)
          sender() ! TaskRunUpdated(name)
      }
    case TaskRunSuccess(name, message) =>
      running().filter(_.name == name) match {
        case Nil => sender() ! RunNotRunning(name)
        case head :: Nil =>
          taskRuns = head.copy(doneAt = Some(DateTime.now), msg = message) :: taskRuns.filterNot(_.name == name)
          sender() ! TaskRunUpdated(name)
      }
    case TaskRunFailure(name, message) =>
      running().filter(_.name == name) match {
        case Nil => sender() ! RunNotRunning(name)
        case head :: Nil =>
          taskRuns = head.copy(doneAt = Some(DateTime.now), msg = message, successful = false) :: taskRuns.filterNot(
            _.name == name
          )
          sender() ! TaskRunUpdated(name)
      }
  }

  def finished() = taskRuns.filter(tr => tr.doneAt.isDefined)

  def running() = taskRuns.filter(tr => tr.startedAt.isDefined && tr.doneAt.isEmpty)

  def pending() = taskRuns.filterNot(_.startedAt.isDefined)
}
