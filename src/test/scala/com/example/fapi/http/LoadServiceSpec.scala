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

import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.example.fapi.data.{Load, LoadRepository}
import de.heikoseeberger.akkahttpcirce.CirceSupport
import org.scalatest.{FreeSpecLike, Matchers}

import concurrent.duration._

// TODO - exchenge ScalatestRouteTest & FreeSpecLike & watch compilation fail
class LoadServiceSpec extends FreeSpecLike with ScalatestRouteTest with Matchers with CirceSupport {

  implicit val timeout: Timeout = 10 seconds
  val repo = system.actorOf(LoadRepository.props(), LoadRepository.Name)

  val service = new LoadService(repo, 10 seconds)
  val route = service.route

  implicit val mat = ActorMaterializer()

  "Load service" - {
    import io.circe.generic.auto._

    "should return all loads" in {
      Get("/load/") ~> route ~> check {
        // TODO - add loads
        responseAs[List[Load]] should be('empty)
      }
    }
    "should return loads for last x sec" in {
    }
    "should return all loads for a single machines" in {
    }
    "should return the most recent loads for all machines" in {
    }
    "should return the most recent loads for a single machine" in {
    }

  }
}
