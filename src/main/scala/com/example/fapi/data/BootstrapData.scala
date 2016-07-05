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
import com.example.fapi.data.TaskRepository.AddTask
import com.example.fapi.data.TaskRunRepository.AddTaskRun
import com.example.fapi.data.sources.LoadGen.{ GenLoad, Purge }
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._

object BootstrapData extends LazyLogging {

  def startGenLoad(loadGen: ActorRef)(implicit actorSystem: ActorSystem) = {
    val scheduler = actorSystem.scheduler
    implicit val executor = actorSystem.dispatcher

    scheduler.schedule(Duration.Zero, 100 millis, loadGen, GenLoad)
    scheduler.schedule(1 second, 5 minutes, loadGen, Purge)
  }

  val initTasks = List("import_db1", "import_db2")

  def storeInitTasks(taskRepo: ActorRef)(implicit actorSystem: ActorSystem) = {
    val scheduler = actorSystem.scheduler
    implicit val executor = actorSystem.dispatcher
    initTasks map { t => taskRepo ! AddTask(t) }
  }

  def storeInitTaskRuns(taskRunGen: ActorRef)(implicit actorSystem: ActorSystem) = {
    val scheduler = actorSystem.scheduler
    implicit val executor = actorSystem.dispatcher

    initTasks foreach { taskName =>
      taskRunGen ! AddTaskRun(taskName)
    }
  }

}
