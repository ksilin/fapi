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

import akka.actor.ActorRef
import akka.http.scaladsl.server.{ Directives, Route }
import akka.pattern.ask
import akka.util.Timeout
import com.example.fapi.data.{ Load, LoadRepository }
import de.heikoseeberger.akkahttpcirce.CirceSupport
import io.swagger.annotations._
import javax.ws.rs.Path

import scala.concurrent.ExecutionContext

@Path("/load") // @Path annotation required for Swagger
@Api(value = "/load", produces = "application/json")
class LoadService(loadRepository: ActorRef, internalTimeout: Timeout)(implicit executionContext: ExecutionContext) extends Directives with CirceSupport {

  import io.circe.generic.auto._

  implicit val timeout = internalTimeout
  val maxPerMachine: Int = 1000

  val route = path("load" /) {
    getLast
  } ~
    path("load" / "last" / IntNumber) { count => getXLast(count) } ~
    path("load" / Segment) { machine => getLastFor(machine) } ~
    path("load" / Segment / "last" / IntNumber) { (machine, count) => getXLastFor(machine, count) }

  @ApiOperation(value = "Get list of all loads", nickname = "getAllLoads", httpMethod = "GET",
    response = classOf[Load], responseContainer = "Set")
  def getLast: Route = get {
    complete {
      (loadRepository ? LoadRepository.GetLastLoads).mapTo[List[Load]]
    }
  }

  def getLastFor(machine: String): Route = get {
    complete {
      (loadRepository ? LoadRepository.GetLastLoadFor(machine)).mapTo[List[Load]]
    }
  }

  def getXLast(count: Int): Route = get {
    complete {
      val c: Int = math.min(count, maxPerMachine)
      (loadRepository ? LoadRepository.GetXLastLoads(c)).mapTo[List[Load]]
    }
  }

  def getXLastFor(machine: String, count: Int): Route = get {
    complete {
      val c: Int = math.min(count, maxPerMachine)
      (loadRepository ? LoadRepository.GetXLastLoadsFor(machine, c)).mapTo[List[Load]]
    }
  }
}
