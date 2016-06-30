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

import akka.actor.{ Actor, ActorLogging, Props }

object StatsRepository {

  case class Stats(name: String)
  case object GetStatss
  case class AddStats(name: String)
  case class StatsAdded(stats: Stats)
  case class StatsExists(name: String)

  final val Name = "stats-repository"
  def props(): Props = Props(new StatsRepository())
}

class StatsRepository extends Actor with ActorLogging {
  import StatsRepository._

  private var statss = Set.empty[Stats]

  override def receive = {
    case GetStatss =>
      log.debug("received GetStatss command")
      sender() ! statss
    case AddStats(name) if statss.exists(_.name == name) =>
      sender() ! StatsExists(name)
    case AddStats(name) =>
      log.info(s"Adding new stats with name; $name")
      val stats = Stats(name)
      statss += stats
      sender() ! StatsAdded(stats)
  }
}
