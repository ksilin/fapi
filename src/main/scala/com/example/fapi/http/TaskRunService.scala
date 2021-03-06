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
import akka.http.scaladsl.model.{ StatusCode, StatusCodes }
import akka.http.scaladsl.server.Directives
import akka.pattern.ask
import akka.util.Timeout
import com.example.fapi.data.TaskRepository.GetTask
import com.example.fapi.data.{ Task, TaskRun, TaskRunRepository }
import de.heikoseeberger.akkahttpcirce.CirceSupport

import scala.concurrent.{ ExecutionContext, Future }

class TaskRunService(taskRunRepository: ActorRef, taskRepository: ActorRef, internalTimeout: Timeout)(implicit executionContext: ExecutionContext) extends Directives with CirceSupport {
  import io.circe.generic.auto._

  implicit val timeout = internalTimeout

  val route = path("taskrun" /) { taskRunGetAll ~ taskRunPost } ~
    path("taskrun" / Segment) { taskName => taskRunsGet(taskName) ~ taskRunDelete(taskName) }

  def taskRunGetAll = get {
    complete {
      (taskRunRepository ? TaskRunRepository.GetAll).mapTo[List[TaskRun]]
    }
  }

  def taskRunGet(runId: String) = get {
    onSuccess((taskRunRepository ? TaskRunRepository.GetTaskRun(runId)).mapTo[List[TaskRun]]) {
      case Nil => complete(StatusCodes.NotFound)
      case x   => complete(x)
    }
  }

  def taskRunDelete(runId: String) = delete {
    onSuccess((taskRunRepository ? TaskRunRepository.GetTaskRun(runId)).mapTo[List[TaskRun]]) {
      case Nil                                     => complete(StatusCodes.NotFound)
      case head :: Nil if head.startedAt.isDefined => complete(StatusCodes.Conflict, "only pending runs can be deleted")
      case head :: Nil if head.startedAt.isEmpty =>
        taskRepository ! TaskRunRepository.Delete(head.id)
        complete(StatusCodes.Accepted)
    }
  }

  def taskRunsGet(taskName: String) = get {
    complete {

      println(s"task name: $taskName")

      val msg = taskName match {
        case "pending"    => TaskRunRepository.GetPending
        case "running"    => TaskRunRepository.GetRunning
        case "finished"   => TaskRunRepository.GetFinished
        case "successful" => TaskRunRepository.GetFinishedSuccessfully
        case "failed"     => TaskRunRepository.GetFailed
        case name         => TaskRunRepository.GetTaskRuns(name)
      }
      (taskRunRepository ? msg).mapTo[List[TaskRun]]
    }
  }

  def taskRunPost = post {
    entity(as[String]) { (taskName: String) =>

      val getTask: Future[List[Task]] = (taskRepository ? GetTask(taskName)).mapTo[List[Task]]

      val res: Future[(StatusCode, String)] = getTask flatMap {
        case Nil => Future.successful((StatusCodes.NotFound, ""))
        case l => taskRunRepository ? TaskRunRepository.AddTaskRun(taskName) map {
          case TaskRunRepository.TaskRunAdded(tr) => (StatusCodes.Created, tr.id)
        }
      }
      complete(res)
    }
  }

}
