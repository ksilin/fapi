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

import akka.pattern.ask // required for ?
import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.CirceSupport

import scala.concurrent.ExecutionContext

class StatsService(statsRepository: ActorRef, internalTimeout: Timeout)(implicit executionContext: ExecutionContext) extends Directives {
  import CirceSupport._
  import io.circe.generic.auto._

  implicit val timeout = internalTimeout

  val route = pathPrefix("stats") { statssGetAll ~ statsPost }

  def statssGetAll = get {
    complete {
      (statsRepository ? StatsRepository.GetStatss).mapTo[Set[StatsRepository.Stats]]
    }
  }

  def statsPost = post {
    entity(as[StatsRepository.Stats]) { stats =>
      onSuccess(statsRepository ? StatsRepository.AddStats(stats.name)) {
        case StatsRepository.StatsAdded(_)  => complete(StatusCodes.Created)
        case StatsRepository.StatsExists(_) => complete(StatusCodes.Conflict)
      }
    }
  }
}
