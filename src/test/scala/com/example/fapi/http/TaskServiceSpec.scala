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
import akka.http.scaladsl.model.ResponseEntity
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.example.fapi.data.{ BootstrapData, Task, TaskRepository }
import de.heikoseeberger.akkahttpcirce.CirceSupport
import org.scalatest.{ BeforeAndAfterAll, FreeSpecLike, Matchers }

import scala.concurrent.duration._

class TaskServiceSpec extends FreeSpecLike with ScalatestRouteTest with Matchers with BeforeAndAfterAll with CirceSupport {

  val repo = system.actorOf(TaskRepository.props(), TaskRepository.Name)
  val taskService = new TaskService(repo, 10 seconds)
  val route = taskService.route
  BootstrapData.storeInitTasks(repo)(system)

  "Task service" - {
    import io.circe.generic.auto._

    "should return all tasks" in {

      Get("/task/") ~> route ~> check {
        status should be(OK)
        contentType should be(`application/json`)
        headers should be(`empty`)
        //        responseAs[String].length should be > 0
        val tasks: List[Task] = responseAs[List[Task]]
        tasks.size should be(BootstrapData.initTasks.size)
      }
    }

    "should add task" in {
      Post("/task/", Task("new_task")) ~> route ~> check {
        status should be(Created)
        contentType should be(`text/plain(UTF-8)`)
        headers should be(`empty`)
        val entity: ResponseEntity = response.entity
        val entStr: String = entity.toString
        println(entStr)
        entStr.length should be > 0
      }
    }

    "should not add existing task" in {
      Post("/task/", Task("import_db1")) ~> route ~> check {
        status should be(Conflict)
        contentType should be(`text/plain(UTF-8)`)
        headers should be(`empty`)
        val entity: ResponseEntity = response.entity
        val entStr: String = entity.toString
        println(entStr)
        entStr.length should be > 0
      }
    }

    "should delete existing task" in {
      Delete("/task/import_db1") ~> route ~> check {
        status should be(Accepted)
        contentType should be(`text/plain(UTF-8)`)
        headers should be(`empty`)
        val entity: ResponseEntity = response.entity
        val entStr: String = entity.toString
        println(entStr)
        entStr.length should be > 0
      }
    }

    "should not delete non-existing task" in {
      Delete("/task/nonexisting") ~> route ~> check {
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
