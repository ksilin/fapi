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
import com.example.fapi.http.ClusterConfig
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration._

object BootstrapData extends LazyLogging with ClusterConfig {

  val initTaskNames = List("import_db1", "import_db2", "import_db3", "gc", "img_convert", "img_inventory")
  implicit val timeout: Timeout = 10 seconds

  def startGenLoad(loadGen: ActorRef)(implicit actorSystem: ActorSystem) = {
    val scheduler = actorSystem.scheduler
    implicit val executor = actorSystem.dispatcher
    logger.debug("started load generation")
    scheduler.schedule(Duration.Zero, 1 second, loadGen, GenLoad)
    scheduler.schedule(1 second, 5 minutes, loadGen, Purge)
  }

  def scheduleTaskRuns(taskRunner: ActorRef)(implicit actorSystem: ActorSystem) = {
    val scheduler = actorSystem.scheduler
    implicit val executor = actorSystem.dispatcher

    scheduler.schedule(Duration.Zero, 30 seconds, taskRunner, StartTask)
  }

  def storeInitTasks(taskRepo: ActorRef)(implicit actorSystem: ActorSystem) = {
    initTaskNames foreach { t =>
      taskRepo ! AddTask(Task(t))
    }
  }

  def storeInitTaskRuns(taskRunRepo: ActorRef)(implicit actorSystem: ActorSystem): (List[TaskRunAdded], List[Any]) = {
    implicit val executor = actorSystem.dispatcher

    val storeRuns: Future[List[TaskRunAdded]] = addPendingRuns(taskRunRepo, initTaskNames)
    val runsStored = Await.result(storeRuns, 10 seconds)

    val startRuns: Future[List[Any]] = {
      val run1: TaskRun = runsStored.head.taskRun
      val run2: TaskRun = runsStored.tail.head.taskRun

      // start and finish two of the pending tasks
      val t1Success = taskRunRepo ? TaskRunStart(run1.name, randomMachine) flatMap (_ =>
        taskRunRepo ? TaskRunSuccess(
          run1.name,
          randomMachine,
          Some("all fine and dandy")
        ))
      val t2Fail = taskRunRepo ? TaskRunStart(run2.name, randomMachine) flatMap (_ =>
        taskRunRepo ? TaskRunFailure(
          run2.name,
          randomMachine,
          Some("kaboom!")
        ))
      val toFinish: Future[List[Any]] = Future.sequence(List(t1Success, t2Fail))
      toFinish
    }
    (runsStored, Await.result(startRuns, 10 seconds))
  }

  def addPendingRuns(
    taskRunRepo: ActorRef,
    taskNames: List[String]
  )(implicit ec: ExecutionContext): Future[List[TaskRunAdded]] = {
    val storeRuns: List[Future[TaskRunAdded]] = taskNames map { taskName =>
      (taskRunRepo ? AddTaskRun(taskName)).mapTo[TaskRunAdded]
    }
    val runsStored = Future.sequence(storeRuns)
    runsStored
  }
}
