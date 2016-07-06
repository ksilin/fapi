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
import akka.http.scaladsl.model.{HttpRequest, ResponseEntity}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.pattern.ask
import akka.util.Timeout
import com.example.fapi.data.TaskRunRepository.{AddTaskRun, GetAll, TaskRunAdded}
import com.example.fapi.data.{BootstrapData, Load, TaskRepository, TaskRun, TaskRunRepository}
import de.heikoseeberger.akkahttpcirce.CirceSupport
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfterAll, FreeSpecLike, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class TaskRunServiceSpec extends FreeSpecLike with ScalatestRouteTest with Matchers with BeforeAndAfterAll with CirceSupport {

  implicit val timeout: Timeout = 10 seconds
  val repo = system.actorOf(TaskRunRepository.props(), TaskRunRepository.Name)
  val taskRepo = system.actorOf(TaskRepository.props(), TaskRepository.Name)
  BootstrapData.storeInitTaskRuns(repo)
  val taskrunService = new TaskRunService(repo, taskRepo, 10 seconds)
  val route = taskrunService.route

  val getRuns = repo ? GetAll
  val runs = Await.result(getRuns, 3 seconds).asInstanceOf[List[TaskRun]]
  val taskName: String = BootstrapData.initTasks.head

  "TaskRun service" - {
    import io.circe.generic.auto._

    "should return all taskruns" in {
      Get("/taskrun/") ~> route ~> check {
        status should be(OK)
        contentType should be(`application/json`)
        headers should be(`empty`)
        val taskruns: List[TaskRun] = responseAs[List[TaskRun]]
        taskruns.size should be(BootstrapData.initTasks.size) // one run per task
      }
    }

    "should return all taskruns for task" in {
      Get(s"/taskrun/${taskName}") ~> route ~> check {
        status should be(OK)
        contentType should be(`application/json`)
        headers should be(`empty`)
        val taskruns: List[TaskRun] = responseAs[List[TaskRun]]
        taskruns.size should be(1)
      }
    }

    // TODO - 404 or empty?
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
      Get(s"/taskrun/${id.get}") ~> route ~> check {
        status should be(OK)
        contentType should be(`application/json`)
        headers should be(`empty`)
        val taskruns: List[TaskRun] = responseAs[List[TaskRun]]
        taskruns.size should be(1)
        taskruns.head.id should be(id)
      }
    }

    "should return 404 if id not found" in {
      Get("/taskrun/123") ~> route ~> check {
        status should be(NotFound)
      }
    }

    "should add taskrun if task known" in {
      val request: HttpRequest = Post("/taskrun/", taskName)
      println(s"sending $request")
      request ~> route ~> check {
        status should be(Created)
        contentType should be(`text/plain(UTF-8)`)
        headers should be(`empty`)
        val entity: ResponseEntity = response.entity
        val entStr: String = entity.toString
        println(entStr)
        entStr.length should be > 0
      }
    }

    "should not add taskrun if task unknown" in {
      Post("/taskrun/", "unknown") ~> route ~> check {
        status should be(NotFound)
      }
    }

    // TODO - on conflict, advise to delete the running instance first
    "should not add taskrun if taskrun for same task unfinished" in {
      Post("/taskrun/", TaskRun(taskName)) ~> route ~> check {
        status should be(Conflict)
        contentType should be(`text/plain(UTF-8)`)
        headers should be(`empty`)
        val entity: ResponseEntity = response.entity
        val entStr: String = entity.toString
        println(entStr)
        entStr.length should be > 0
      }
    }

    // TODO - DELETE appropriate for cancelling?
    "should delete existing taskrun" in {
      Delete("/taskrun/", TaskRun(taskName)) ~> route ~> check {
        status should be(Accepted)
        contentType should be(`text/plain(UTF-8)`)
        headers should be(`empty`)
        val entity: ResponseEntity = response.entity
        val entStr: String = entity.toString
        println(entStr)
        entStr.length should be > 0
      }
    }

    "should not delete non-existing taskrun" in {
      Delete("/taskrun/", TaskRun("nonexisting")) ~> route ~> check {
        status should be(NotFound)
        contentType should be(`text/plain(UTF-8)`)
        headers should be(`empty`)
        val entity: ResponseEntity = response.entity
        val entStr: String = entity.toString
        println(entStr)
        entStr.length should be > 0
      }
    }

    // TODO - put

    // TODO - test rejection: http://doc.akka.io/docs/akka/2.4.7/scala/http/routing-dsl/testkit.html
  }
}
