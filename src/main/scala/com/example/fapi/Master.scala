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

package com.example.fapi

import akka.actor.{ Actor, ActorLogging, ActorRef, SupervisorStrategy, Terminated }
import com.example.fapi.data.{ BootstrapData, LoadRepository, TaskRepository, TaskRunRepository, TaskRunner }
import com.example.fapi.http.HttpService

import concurrent.duration._

class Master extends Actor with ActorLogging {
  override val supervisorStrategy = SupervisorStrategy.stoppingStrategy

  private val loadRepository = context.watch(createLoadRepository())
  private val taskRepository = context.watch(createTaskRepository())
  private val taskRunRepository = context.watch(createTaskRunRepository())
  context.watch(createHttpService(loadRepository, taskRepository, taskRunRepository))

  private val taskRunner = context.watch(createTaskRunner(taskRunRepository))

  BootstrapData.startGenLoad(loadRepository)(context.system)
  BootstrapData.storeInitTasks(taskRepository)(context.system)
  BootstrapData.storeInitTaskRuns(taskRunRepository)(context.system)
  BootstrapData.scheduleTaskRuns(taskRunner)(context.system)

  log.info("Up and running")

  override def receive = {
    case Terminated(actor) => onTerminated(actor)
  }

  protected def createLoadRepository(): ActorRef = {
    context.actorOf(LoadRepository.props(), LoadRepository.Name)
  }

  protected def createTaskRepository(): ActorRef = {
    context.actorOf(TaskRepository.props(), TaskRepository.Name)
  }

  protected def createTaskRunner(taskRunRepo: ActorRef): ActorRef = {
    context.actorOf(TaskRunner.props(taskRunRepo), TaskRunner.Name)
  }

  protected def createTaskRunRepository(): ActorRef = {
    context.actorOf(TaskRunRepository.props(), TaskRunRepository.Name)
  }

  protected def createHttpService(loadRepository: ActorRef, taskRepository: ActorRef, taskRunRepository: ActorRef): ActorRef = {
    val selfTimeout = 10 seconds

    context.actorOf(
      HttpService.props(selfTimeout, loadRepository, taskRepository, taskRunRepository),
      HttpService.Name
    )
  }

  protected def onTerminated(actor: ActorRef): Unit = {
    log.error("Terminating the system because {} terminated!", actor)
    context.system.terminate()
  }
}
