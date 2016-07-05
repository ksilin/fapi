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

object TaskRepository {

  case object GetAll
  case class GetTask(name: String)
  case class AddTask(name: String)
  case class TaskAdded(task: Task)
  case class TaskExists(name: String)
  case class DeleteTask(name: String)
  case class TaskWillBeDeleted(name: String)
  case class TaskNotFound(name: String)

  final val Name = "task-repository"
  def props(): Props = Props(new TaskRepository())
}

class TaskRepository extends Actor with ActorLogging {
  import TaskRepository._

  private var tasks = List(Task("import db1"), Task("import db2"))

  override def receive = {
    case GetAll =>
      log.debug("received GetTasks command")
      sender() ! tasks
    case AddTask(name) if tasks.exists(_.name == name) =>
      sender() ! TaskExists(name)
    case AddTask(name) =>
      log.info(s"Adding new task with name $name")
      val task = Task(name)
            tasks = task :: tasks
              sender() ! TaskAdded(task)

    case DeleteTask(name) if tasks.exists(_.name == name) =>
      // TODO - add deletion handling
      tasks = tasks.filterNot(_.name == name)
      sender() ! TaskWillBeDeleted(name)
    case DeleteTask(name) =>
      log.info(s"Adding new task with name $name")
      sender() ! TaskNotFound(name)

  }
}