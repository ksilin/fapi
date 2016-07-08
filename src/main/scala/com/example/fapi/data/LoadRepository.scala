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

package com.example.fapi.data

import akka.actor.{ Actor, ActorLogging, Props }
import com.example.fapi.http.ClusterConfig
import org.joda.time.DateTime

object LoadRepository {

  case object GetLastLoads
  case class GetXLastLoads(count: Int)
  case class GetLastLoadFor(machine: String)
  case class GetXLastLoadsFor(machine: String, count: Int)
  case class GetLoadStartingAt(t: DateTime)
  case class GetLoadStartingAtFor(t: DateTime, machine: String)
  case class DeleteLoadsBefore(t: DateTime)
  case class StoreLoad(load: Load)

  final val Name = "load-repository"
  def props(): Props = Props(new LoadRepository())

}

class LoadRepository extends Actor with ActorLogging with ClusterConfig {
  import LoadRepository._

  override def receive = {
    case GetLastLoads =>
      log.debug("received GetLoad command")
      val lastLoads: List[Load] = machines flatMap { machine => h2DB.run(xlastLoadsFor)(machine, 1) }
      sender() ! lastLoads
    case GetLastLoadFor(machine: String) =>
      log.debug(s"received GetLoadFor $machine command")
      val lastLoads: List[Load] = h2DB.run(xlastLoadsFor)(machine, 1)
      sender() ! lastLoads
    case GetXLastLoads(count: Int) =>
      log.debug("received GetLoad command")
      val lastLoads: List[Load] = machines flatMap { machine => h2DB.run(xlastLoadsFor)(machine, count) }
      sender() ! lastLoads
    case GetXLastLoadsFor(machine: String, count: Int) =>
      log.debug(s"received GetLoadFor $machine command")
      val lastLoads: List[Load] = h2DB.run(xlastLoadsFor)(machine, count)
      sender() ! lastLoads
    case GetLoadStartingAt(t: DateTime) =>
      log.debug(s"received GetLoadStartingAt(${t}) command")
      val loads = machines flatMap { machine => h2DB.run(loadsAfterFor)(t, machine) }
      sender() ! loads
    case GetLoadStartingAtFor(t: DateTime, machine: String) =>
      log.debug(s"received GetLoadStartingAtFor($t, $machine) command")
      val loads = h2DB.run(loadsAfterFor)(t, machine)
      sender() ! loads
    case StoreLoad(load: Load) =>
      log.debug(s"received StoreLoad(${load}) command")
      val loads: List[Long] = h2DB.run(insertload)(List(load))
      sender() ! loads
    case DeleteLoadsBefore(t: DateTime) =>
      log.debug(s"received DeleteLoadsBefore $t command")
      val deleted: List[Long] = h2DB.run(deleteLoadsBefore)(List(t))
      sender() ! deleted
  }
}
