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

package com.example.data

import akka.actor.{ Actor, ActorLogging, Props }
import org.joda.time.DateTime

object LoadRepository {

  case object GetLoad
  case class GetLoadStartingAt(t: DateTime)

  final val Name = "load-repository"
  def props(): Props = Props(new LoadRepository())

}

class LoadRepository extends Actor with ActorLogging {
  import LoadRepository._

  // TODO - init state

  override def receive = {
    case GetLoad =>
      log.debug("received GetLoad command")
      val getAll: List[Load] = testH2DB.run(loads)
      sender() ! getAll
    case GetLoadStartingAt(t: DateTime) =>
      log.debug(s"received GetLoadStartingAt(${t: DateTime}) command")
  }
}
