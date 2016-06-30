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

import akka.actor._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object DataApp {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("dataappsys")

    system.actorOf(Props(new Master), "dataapp-master")

    Await.ready(system.whenTerminated, Duration.Inf)
  }

}
