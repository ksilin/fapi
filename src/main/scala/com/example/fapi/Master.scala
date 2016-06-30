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
import com.example.data.{ LoadRepository, TaskRepository }

import concurrent.duration._

class Master extends Actor with ActorLogging {
  override val supervisorStrategy = SupervisorStrategy.stoppingStrategy

  private val loadRepository = context.watch(createLoadRepository())
  private val taskRepository = context.watch(createTaskRepository())
  context.watch(createHttpService(loadRepository, taskRepository))

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

  protected def createHttpService(loadRepositoryActor: ActorRef, taskRepositoryActor: ActorRef): ActorRef = {
    val address = "127.0.0.1"
    val port = 9001
    val selfTimeout = 10 seconds

    context.actorOf(
      HttpService.props(address, port, selfTimeout, loadRepositoryActor, taskRepositoryActor),
      HttpService.Name
    )
  }

  protected def onTerminated(actor: ActorRef): Unit = {
    log.error("Terminating the system because {} terminated!", actor)
    context.system.terminate()
  }
}