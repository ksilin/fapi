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
import akka.testkit.{ TestActorRef, TestKit }
import akka.util.Timeout
import akka.pattern.ask
import com.example.fapi.data.TaskRunRepository.{ TaskRunStart, _ }
import com.example.fapi.http.ClusterConfig
import com.typesafe.scalalogging.LazyLogging
import org.scalatest._

import scala.concurrent.duration._

class TaskRunRepositorySpec
    extends TestKit(ActorSystem("TaskRunRepoSpec"))
    with AsyncFreeSpecLike
    with Matchers
    with BeforeAndAfterAll
    with LazyLogging
    with ClusterConfig {

  implicit val timeout: Timeout = 10 seconds
  val taskRunRepo = TestActorRef(new TaskRunRepository)
  val taskRepo = TestActorRef(new TaskRepository)

  BootstrapData.storeInitTasks(taskRepo)
  // a pending run for each task plus 1 failed and 1 succeeded
  val (startedRuns, finishedRuns) = BootstrapData.storeInitTaskRuns(taskRunRepo)
  val initTaskCount: Int = BootstrapData.initTaskNames.size

  "TaskRunRepository" - {

    "should return all task runs" in {
      val getAllRuns = (taskRunRepo ? GetAll).mapTo[List[TaskRun]]
      getAllRuns.map { (runs: List[TaskRun]) =>
        runs foreach println
        runs.size shouldBe (initTaskCount)
      }
    }

    "should return all pending runs" in {
      val pending = (taskRunRepo ? GetPending).mapTo[List[TaskRun]]
      pending map { trs =>
        println(s"pending $trs")
        trs.size should be(startedRuns.size - finishedRuns.size)
      }
    }

    "should return all running tasks" in {
      val running = (taskRunRepo ? GetRunning).mapTo[List[TaskRun]]
      running map { trs =>
        trs.size should be(0)
      }
    }

    "should return all finished runs" in {
      val finished = (taskRunRepo ? GetFinished).mapTo[List[TaskRun]]
      finished map { trs =>
        trs.size should be(finishedRuns.size)
      }
    }

    "should return all successfully finished runs" in {
      val success = (taskRunRepo ? GetFinishedSuccessfully).mapTo[List[TaskRun]]
      success map { trs =>
        trs.size should be(1)
      }
    }
    "should return all failed runs" in {
      val failed = (taskRunRepo ? GetFailed).mapTo[List[TaskRun]]
      failed map { trs =>
        trs.size should be(1)
      }
    }

    "should start pending run" in {

      val pending = (taskRunRepo ? GetPending).mapTo[List[TaskRun]]

      pending flatMap { (trs: List[TaskRun]) =>
        println(s"pending $trs")
        val name: String = trs.head.name
        ((taskRunRepo ? TaskRunStart(name)).mapTo[TaskRunUpdated]).map(r => (r, name))
      } map {
        case (r, name) =>
          r should be(TaskRunUpdated(name))
      }
    }

    "should ignore start if task running" in {
      val running = (taskRunRepo ? GetRunning).mapTo[List[TaskRun]]

      running flatMap { (trs: List[TaskRun]) =>
        println(s"running $trs")
        val name: String = trs.head.name
        ((taskRunRepo ? TaskRunStart(name)).mapTo[RunNotPending]).map(r => (r, name))
      } map {
        case (r, name) =>
          r should be(RunNotPending(name))
      }
    }

    "should ignore start if task finished" in {
      val finished = (taskRunRepo ? GetFinished).mapTo[List[TaskRun]]

      finished flatMap { (trs: List[TaskRun]) =>
        println(s"finished $trs")
        val name: String = trs.head.name
        (taskRunRepo ? TaskRunStart(name)).mapTo[RunNotPending].map(r => (r, name))
      } map {
        case (r, name) =>
          r should be(RunNotPending(name))
      }
    }

    "should successfully finish running tasks" in {
      val running = (taskRunRepo ? GetRunning).mapTo[List[TaskRun]]

      running flatMap { (trs: List[TaskRun]) =>
        println(s"running $trs")
        val name: String = trs.head.name
        (taskRunRepo ? TaskRunSuccess(name)).mapTo[TaskRunUpdated].map(r => (r, name))
      } map {
        case (r, name) =>
          r should be(TaskRunUpdated(name))
      }
    }

    "should ignore finish pending tasks" in {
      val pending = (taskRunRepo ? GetPending).mapTo[List[TaskRun]]

      pending flatMap { (trs: List[TaskRun]) =>
        println(s"pending $trs")
        val name: String = trs.head.name
        (taskRunRepo ? TaskRunSuccess(name)).mapTo[RunNotRunning].map(r => (r, name))
      } map {
        case (r, name) =>
          r should be(RunNotRunning(name))
      }
    }

    "should ignore finish finished tasks" in {
      val finished = (taskRunRepo ? GetFinished).mapTo[List[TaskRun]]

      finished flatMap { (trs: List[TaskRun]) =>
        println(s"finished $trs")
        val name: String = trs.head.name
        (taskRunRepo ? TaskRunSuccess(name)).mapTo[RunNotRunning].map(r => (r, name))
      } map {
        case (r, name) =>
          r should be(RunNotRunning(name))
      }
    }
  }
}
