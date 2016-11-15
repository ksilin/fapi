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

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.example.fapi.data.TaskRepository.AddTask
import com.example.fapi.data.TaskRunRepository.AddTaskRun
import com.example.fapi.data.sources.LoadGen.{ GenLoad, Purge }
import org.scalatest.{ FreeSpecLike, Matchers }

class BootstrapDataSpec extends TestKit(ActorSystem("bootstrapDataSpecSystem")) with FreeSpecLike with Matchers {

  import system.dispatcher

  "test creation of initial data" - {

    "starts load creation" in {
      BootstrapData.startGenLoad(testActor)
      expectMsgAnyOf(GenLoad, Purge)
      expectMsgAnyOf(GenLoad, Purge)
    }

    "adds initial tasks" in {
      BootstrapData.storeInitTasks(testActor)
      val expTasks: List[AddTask] = BootstrapData.initTaskNames map (name => AddTask(Task(name)))
      (0 until expTasks.size).foreach { _ =>
        expectMsgAnyOf(expTasks: _*)
      }
    }

    "adds runs for initial tasks" in {
      val testtask: String = "testtask"
      BootstrapData.addPendingRuns(testActor, List(testtask))
      expectMsg(AddTaskRun(testtask))
    }

  }
}
