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
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.example.fapi.data.{Task, TaskRepository}
import de.heikoseeberger.akkahttpcirce.CirceSupport
import org.scalatest.{BeforeAndAfterAll, FreeSpecLike, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._
// extends TestKit(ActorSystem("taskServiceSpec"))
class TaskServiceSpec extends FreeSpecLike with ScalatestRouteTest with Matchers with BeforeAndAfterAll with CirceSupport {
  import io.circe.generic.auto._

  val repo = system.actorOf(TaskRepository.props(), TaskRepository.Name)
  val taskService = new TaskService(repo, 10 seconds)
  val route = taskService.route

  // TODO - the table is created, do fill it
//  override def beforeAll() = {
//    repo.createTable()
//    Await.result(repo.store(List(rec)), 5 seconds)
//  }
//  override def afterAll() = cass.close()

  "Task service" - {

    val respContent = "sdgf"

    "should return payload of found Record" in {
      Get("/task/") ~> route ~> check {
        status should be(OK)
        contentType should be(`application/json`)
        headers should be(`empty`)
        responseAs[String].length should be > 0
        val tasks: List[Task] = responseAs[List[Task]]
        tasks.size should be(2)
      }
    }

    // TODO - post & put



    // TODO - test rejection: http://doc.akka.io/docs/akka/2.4.7/scala/http/routing-dsl/testkit.html
  }
}
