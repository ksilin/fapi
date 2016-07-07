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

import akka.actor.{ ActorRef, ActorSystem }
import akka.pattern.ask
import akka.util.Timeout
import com.example.fapi.data.TaskRepository.AddTask
import com.example.fapi.data.TaskRunRepository._
import com.example.fapi.data.TaskRunner.StartTask
import com.example.fapi.data.sources.LoadGen.{ GenLoad, Purge }
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

object BootstrapData extends LazyLogging {

  def startGenLoad(loadGen: ActorRef)(implicit actorSystem: ActorSystem) = {
    val scheduler = actorSystem.scheduler
    implicit val executor = actorSystem.dispatcher

    scheduler.schedule(Duration.Zero, 100 millis, loadGen, GenLoad)
    scheduler.schedule(1 second, 5 minutes, loadGen, Purge)
  }

  def scheduleTaskRuns(taskRunner: ActorRef)(implicit actorSystem: ActorSystem) = {
    val scheduler = actorSystem.scheduler
    implicit val executor = actorSystem.dispatcher

    scheduler.schedule(Duration.Zero, 30 seconds, taskRunner, StartTask)
  }

  val initTasks = List("import_db1", "import_db2")
  implicit val timeout: Timeout = 10 seconds

  def storeInitTasks(taskRepo: ActorRef)(implicit actorSystem: ActorSystem) = {
    initTasks foreach { t => taskRepo ! AddTask(t) }
  }

  def storeInitTaskRuns(taskRunRepo: ActorRef)(implicit actorSystem: ActorSystem) = {
    implicit val executor = actorSystem.dispatcher

    val storeRuns: List[Future[TaskRunAdded]] = initTasks map { taskName =>
      (taskRunRepo ? AddTaskRun(taskName)).mapTo[TaskRunAdded]
    }
    val runsStored = Future.sequence(storeRuns)

    val waitForIt: Future[List[Any]] = runsStored flatMap { runs =>
      val run1: TaskRun = runs.head.taskRun
      val run2: TaskRun = runs.tail.head.taskRun

      val t1Run = taskRunRepo ? TaskRunStart(run1.id.get)
      val t2Success = taskRunRepo ? TaskRunStart(run2.id.get) map (_ => taskRunRepo ? TaskRunSuccess(run2.id.get, Some("all fine and dandy")))
      Future.sequence(List(t1Run, t2Success))
    }
    Await.result(waitForIt, 10 seconds)

    val storeMoreRuns: List[Future[TaskRunAdded]] = initTasks map { taskName =>
      (taskRunRepo ? AddTaskRun(taskName)).mapTo[TaskRunAdded]
    }
    val moreRunsStored = Future.sequence(storeMoreRuns)
    val moreRuns: List[TaskRunAdded] = Await.result(moreRunsStored, 10 seconds)

    val run3 = moreRuns.head.taskRun
    val t2Fail: Future[Any] = taskRunRepo ? TaskRunStart(run3.id.get) flatMap (_ => taskRunRepo ? TaskRunFailure(run3.id.get, Some("kaboom!")))
    Await.result(t2Fail, 10 seconds)
  }

}
