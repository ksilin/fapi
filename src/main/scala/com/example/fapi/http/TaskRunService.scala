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
import com.example.fapi.data.{ Task, TaskRepository, TaskRun, TaskRunRepository }
import de.heikoseeberger.akkahttpcirce.CirceSupport

import scala.concurrent.ExecutionContext

class TaskRunService(taskRunRepository: ActorRef, internalTimeout: Timeout)(implicit executionContext: ExecutionContext) extends Directives with CirceSupport {
  import io.circe.generic.auto._

  implicit val timeout = internalTimeout

  val route = pathPrefix("taskrun") { pathSingleSlash { taskRunGetAll ~ taskRunPost } }

  def taskRunGetAll = get {
    complete {
      (taskRunRepository ? TaskRunRepository.GetAll).mapTo[List[TaskRun]]
    }
  }

  def taskRunPost = post {
    entity(as[Task]) { (taskRun: Task) =>
      onSuccess(taskRunRepository ? TaskRepository.AddTask(taskRun.name)) {
        case TaskRunRepository.TaskRunAdded(_) => complete(StatusCodes.Created)
        //        case TaskRunRepository.TaskExists(_) => complete(StatusCodes.Conflict)
      }
    }
  }

}
