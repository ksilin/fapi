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

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.pattern.ask
import akka.util.Timeout
import com.example.fapi.data.TaskRunRepository.{ GetPending, TaskRunStart, TaskRunSuccess }

import scala.concurrent.duration._

object TaskRunner {

  case object StartTask

  case object EndTaskSuccess

  case object EndTaskFail

  case class TaskRunStarted(taskRunId: Integer)

  case class TaskRunEnded(taskRunId: Integer)

  def props(taskRunRepo: ActorRef): Props = Props(new TaskRunner(taskRunRepo))

  val Name = "taskrunner"
}

class TaskRunner(taskRunRepo: ActorRef) extends Actor with ActorLogging {

  import TaskRunner._

  implicit val timeout: Timeout = 10 seconds
  implicit val ec = context.dispatcher
  val taskRunTime = 30 seconds

  override def receive = {
    case StartTask =>
      log.debug("received StartTask command")
      (taskRunRepo ? GetPending).mapTo[List[TaskRun]] map { runs =>
        runs.headOption map { run =>
          val id: Int = run.id.get
          taskRunRepo ! TaskRunStart(id)
          // TODO - schedule failing tasks as well
          context.system.scheduler.scheduleOnce(taskRunTime, taskRunRepo, TaskRunSuccess(id, Some("all is well")))
        }
      }
  }
}
