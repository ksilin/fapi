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

import akka.http.scaladsl.model.{ ContentTypes, StatusCodes }
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.pattern.ask
import com.example.fapi.data.{ Load, LoadRepository }
import de.heikoseeberger.akkahttpcirce.CirceSupport
import org.joda.time.DateTime
import org.scalatest.{ FreeSpecLike, Matchers }

import concurrent.duration._
import scala.concurrent.{ Await, Future }

// TODO - change places - ScalatestRouteTest & FreeSpecLike & watch compilation fail
class LoadServiceSpec extends FreeSpecLike with ScalatestRouteTest with Matchers with CirceSupport with ClusterConfig {

  implicit val timeout: Timeout = 10 seconds
  val repo = system.actorOf(LoadRepository.props(), LoadRepository.Name)

  val service = new LoadService(repo, 10 seconds)
  val route = service.route

  implicit val mat = ActorMaterializer()
  val perMachine = 3

  private val loadCount: Int = 2
  val getRes: List[Future[Any]] = machines flatMap { machine =>
    (0 until perMachine).to[List] map { i =>
      val load = Load(machine, i * 10, i * 5, math.pow(10, i).toInt, DateTime.now().minus(i * 1000))
      repo ? LoadRepository.StoreLoad(load)
    }
  }
  val res = Future.sequence(getRes)
  val stored = Await.result(res, 10 seconds)
  println(s"stored loads: $stored")

  "Load service" - {
    import io.circe.generic.auto._

    "should return most recent loads for all machines" in {
      Get("/load/") ~> route ~> check {
        status should be(StatusCodes.OK)
        contentType should be(ContentTypes.`application/json`)
        responseAs[List[Load]].size should be(machines.size)
      }
    }

    "should return last x loads for all machines" in {
      Get(s"/load/last/$loadCount") ~> route ~> check {
        status should be(StatusCodes.OK)
        contentType should be(ContentTypes.`application/json`)
        responseAs[List[Load]].size should be(machines.size * loadCount)
      }
    }

    "should return most recent load for a single machines" in {
      Get(s"/load/${machines.head}") ~> route ~> check {
        status should be(StatusCodes.OK)
        contentType should be(ContentTypes.`application/json`)
        responseAs[List[Load]].size should be(1)
      }
    }

    "should return last x loads for a single machines" in {
      Get(s"/load/${machines.head}/last/$loadCount") ~> route ~> check {
        status should be(StatusCodes.OK)
        contentType should be(ContentTypes.`application/json`)
        responseAs[List[Load]].size should be(loadCount)
      }
    }

    // TODO - cap to 1K and test it
  }
}
