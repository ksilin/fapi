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

package com.example.fapi.http

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.pattern.ask
import akka.util.Timeout
import com.example.fapi.data.{ Task, TaskRepository }
import de.heikoseeberger.akkahttpcirce.CirceSupport

import scala.concurrent.ExecutionContext

class TaskService(taskRepository: ActorRef, internalTimeout: Timeout)(implicit executionContext: ExecutionContext) extends Directives with CirceSupport {
  import io.circe.generic.auto._

  implicit val timeout = internalTimeout

  val route = pathPrefix("task") { taskGetAll ~ taskPost ~ deleteTask }

  def taskGetAll = get {
    complete {
      (taskRepository ? TaskRepository.GetAll).mapTo[List[Task]]
    }
  }

  def taskPost = post {
    entity(as[Task]) { (task: Task) =>
      onSuccess(taskRepository ? TaskRepository.AddTask(task.name)) {
        case TaskRepository.TaskAdded(_)  => complete(StatusCodes.Created)
        case TaskRepository.TaskExists(_) => complete(StatusCodes.Conflict)
      }
    }
  }

  def deleteTask = delete {
    entity(as[Task]) { (task: Task) =>
      onSuccess(taskRepository ? TaskRepository.DeleteTask(task.name)) {
        case TaskRepository.TaskWillBeDeleted(_)  => complete(StatusCodes.Accepted)
        case TaskRepository.TaskNotFound(_) => complete(StatusCodes.NotFound)
      }
    }
  }
}
