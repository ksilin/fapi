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

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props, Status }
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.directives.{ Credentials, DebuggingDirectives }
import akka.http.scaladsl.server.{ Directives, Route }
import akka.http.scaladsl.model.HttpMethods._
import akka.pattern.pipe
import akka.stream.{ ActorMaterializer, Materializer }
import akka.util.Timeout
import ch.megard.akka.http.cors.CorsDirectives._
import ch.megard.akka.http.cors.CorsSettings

import scala.concurrent.ExecutionContext

object HttpService extends HttpConfig {

  final val Name = "http-service"

  def props(
    internalTimeout: Timeout,
    loadRepository: ActorRef,
    taskRepository: ActorRef,
    taskRunRepository: ActorRef
  ): Props =
    Props(new HttpService(httpInterface, httpPort, internalTimeout, loadRepository, taskRepository, taskRunRepository))

  private def route(
    httpService: ActorRef,
    internalTimeout: Timeout,
    loadRepository: ActorRef,
    taskRepository: ActorRef,
    taskRunRepository: ActorRef,
    system: ActorSystem
  )(implicit ec: ExecutionContext, mat: Materializer) = {
    import Directives._

    def assets = pathPrefix("swagger") {
      getFromResourceDirectory("swagger") ~ pathSingleSlash(get(redirect("index.html", StatusCodes.PermanentRedirect)))
    }

    def simplePassAuth(credentials: Credentials): Option[String] =
      credentials match {
        case p @ Credentials.Provided(id) =>
          authMap.get(id).filter(p.verify).map(_ => id)
        case _ => None
      }

    val loadRoute: Route = new LoadService(loadRepository, internalTimeout).route
    val taskRoute: Route = new TaskService(taskRepository, internalTimeout).route
    val taskRunRoute: Route = new TaskRunService(taskRunRepository, taskRepository, internalTimeout).route
    val swaggerDocRoute: Route = new SwaggerDocService(httpInterface, httpPort, system).routes
    val fullRoute = assets ~ loadRoute ~ taskRoute ~ taskRunRoute ~ swaggerDocRoute

    val authRoute: Route = authenticateBasic(realm = "fapi", simplePassAuth) { userName =>
      fullRoute
    }

    val corsSettings = CorsSettings.defaultSettings.copy(allowedMethods = List(GET, PUT, POST, HEAD, DELETE, OPTIONS))

    val corsRoute = cors(corsSettings) { authRoute }

    DebuggingDirectives.logRequest("request", Logging.DebugLevel)(corsRoute)
  }
}

class HttpService(
  address: String,
  port: Int,
  internalTimeout: Timeout,
  loadRepository: ActorRef,
  taskRepository: ActorRef,
  taskRunRepository: ActorRef
)
    extends Actor
    with ActorLogging {

  import HttpService._
  import context.dispatcher

  private implicit val mat = ActorMaterializer()

  Http(context.system)
    .bindAndHandle(
      route(self, internalTimeout, loadRepository, taskRepository, taskRunRepository, context.system),
      address,
      port
    )
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
