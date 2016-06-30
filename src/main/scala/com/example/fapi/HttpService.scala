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

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props, Status }
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.directives.DebuggingDirectives
import akka.http.scaladsl.server.{ Directives, Route }
import akka.stream.{ ActorMaterializer, Materializer }
import akka.util.Timeout
import akka.pattern.pipe

import scala.concurrent.ExecutionContext

object HttpService {

  final val Name = "http-service"

  def props(address: String, port: Int, internalTimeout: Timeout, statsRepository: ActorRef): Props =
    Props(new HttpService(address, port, internalTimeout, statsRepository))

  private def route(httpService: ActorRef, address: String, port: Int, internalTimeout: Timeout,
    statsRepository: ActorRef, system: ActorSystem)(implicit ec: ExecutionContext, mat: Materializer) = {
    import Directives._
    import io.circe.generic.auto._

    def assets = pathPrefix("swagger") {
      getFromResourceDirectory("swagger") ~ pathSingleSlash(get(redirect("index.html", StatusCodes.PermanentRedirect)))
    }

    val fullRoute = assets ~ new StatsService(statsRepository, internalTimeout).route
    val clientRequestLogged: Route = DebuggingDirectives.logRequest("request", Logging.DebugLevel)(fullRoute)
    clientRequestLogged
  }
}

class HttpService(address: String, port: Int, internalTimeout: Timeout, statsRepository: ActorRef)
    extends Actor with ActorLogging {

  import HttpService._
  import context.dispatcher

  private implicit val mat = ActorMaterializer()

  Http(context.system)
    .bindAndHandle(route(self, address, port, internalTimeout, statsRepository, context.system), address, port)
    .pipeTo(self)

  override def receive = binding

  private def binding: Receive = {
    case serverBinding @ Http.ServerBinding(address) =>
      log.info("Listening on {}", address)

    case Status.Failure(cause) =>
      log.error(cause, s"Can't bind to $address:$port")
      context.stop(self)
  }
}
