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

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.{ HttpRequest, ResponseEntity }
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.example.fapi.data.TaskRunRepository._
import com.example.fapi.data.{ BootstrapData, TaskRepository, TaskRun, TaskRunRepository }
import de.heikoseeberger.akkahttpcirce.CirceSupport
import org.scalatest.{ BeforeAndAfterAll, FreeSpecLike, Matchers }

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

class TaskRunServiceSpec extends FreeSpecLike with ScalatestRouteTest with Matchers with BeforeAndAfterAll with CirceSupport {

  implicit val timeout: Timeout = 10 seconds
  val taskRunRepo = system.actorOf(TaskRunRepository.props(), TaskRunRepository.Name)
  val taskRepo = system.actorOf(TaskRepository.props(), TaskRepository.Name)

  implicit val mat = ActorMaterializer()

  BootstrapData.storeInitTasks(taskRepo)
  val (startedRuns, finishedRuns) = BootstrapData.storeInitTaskRuns(taskRunRepo)

  val taskrunService = new TaskRunService(taskRunRepo, taskRepo, 10 seconds)
  val route = taskrunService.route

  val getRuns = (taskRunRepo ? GetAll).mapTo[List[TaskRun]]
  val initTaskCount: Int = BootstrapData.initTaskNames.size
  val runs: List[TaskRun] = Await.result(getRuns, 3 seconds)
  val taskName: String = BootstrapData.initTaskNames.head // TODO - extract number of stored task runs here

  "TaskRun service" - {
    import io.circe.generic.auto._

    "should return all taskruns" in {
      Get("/taskrun/") ~> route ~> check {
        status should be(OK)
        contentType should be(`application/json`)
        headers should be(`empty`)
        val taskruns: List[TaskRun] = responseAs[List[TaskRun]]
        taskruns.size should be(initTaskCount)
      }
    }

    "should return all taskruns for task" in {
      Get(s"/taskrun/${taskName}") ~> route ~> check {
        status should be(OK)
        contentType should be(`application/json`)
        headers should be(`empty`)
        val taskruns: List[TaskRun] = responseAs[List[TaskRun]]
        println(taskruns)
        taskruns.size should be(1)
      }
    }

    "should return 404 if task not found" in {
      Get(s"/taskrun/gobbledigook") ~> route ~> check {
        status should be(OK)
        contentType should be(`application/json`)
        headers should be(`empty`)
        val taskruns: List[TaskRun] = responseAs[List[TaskRun]]
        taskruns should be('empty)
      }
    }

    "should return taskrun by id" in {
      val id = runs.head.id
      Get(s"/taskrun/${id}") ~> route ~> check {
        status should be(OK)
        contentType should be(`application/json`)
        headers should be(`empty`)
        val taskruns: List[TaskRun] = responseAs[List[TaskRun]]
        taskruns.size should be(1)
        taskruns.head.id should be(id)
      }
    }

    "should return 404 if id not found" in {
      Get("/taskrun/doesnotexist") ~> route ~> check {
        status should be(NotFound)
      }
    }

    "should add taskrun if task known" in {
      val request: HttpRequest = Post("/taskrun/", taskName)
      println(s"sending $request")
      request ~> route ~> check {
        status should be(Created)
        contentType should be(`application/json`)
        headers should be(`empty`)
        val rs = responseAs[String]
        println(s"direct: $rs")
      }
    }

    "should not add taskrun if task unknown" in {
      Post("/taskrun/", "unknown") ~> route ~> check {
        status should be(NotFound)
      }
    }

    "should delete pending taskrun" in {
      val getPending: Future[List[TaskRun]] = (taskRunRepo ? GetPending).mapTo[List[TaskRun]]
      val pending = Await.result(getPending, 10 seconds)
      val runId: String = pending.head.id
      Delete(s"/taskrun/$runId") ~> route ~> check {
        status should be(Accepted)
        contentType should be(`text/plain(UTF-8)`)
        headers should be(`empty`)
      }
    }

    "should not delete non-existing taskrun" in {
      Delete("/taskrun/123") ~> route ~> check {
        status should be(NotFound)
      }
    }

    "should not delete finished taskrun" in {
      val getFinished: Future[List[TaskRun]] = (taskRunRepo ? GetFinished).mapTo[List[TaskRun]]
      val finished = Await.result(getFinished, 10 seconds)
      val runId: String = finished.head.id
      Delete(s"/taskrun/$runId") ~> route ~> check {
        status should be(Conflict)
        contentType should be(`application/json`)
        headers should be(`empty`)
        responseAs[String] should be("only pending runs can be deleted")
      }
    }
  }
}
