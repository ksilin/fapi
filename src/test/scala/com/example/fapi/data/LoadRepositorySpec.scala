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

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.{ TestActorRef, TestKit }
import akka.util.Timeout
import com.example.fapi.data.LoadRepository._
import com.example.fapi.http.ClusterConfig
import com.typesafe.scalalogging.LazyLogging
import org.joda.time.DateTime
import org.scalatest.{ AsyncFreeSpecLike, BeforeAndAfterAll, Matchers }

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

class LoadRepositorySpec extends TestKit(ActorSystem("LoadRepoSpec")) with AsyncFreeSpecLike with Matchers
    with BeforeAndAfterAll with LazyLogging with ClusterConfig {

  implicit val timeout: Timeout = 10 seconds
  val repo = TestActorRef(new LoadRepository)

  private val count: Int = 4

  // TODO - beforeAll runs multiple times - what gives?

  override def beforeAll() = {
    logger.info("-----------------------")
    logger.info(s"beforeAll running!")
    logger.info("-----------------------")
    val reps: List[Int] = (0 until count).to[List]
    val stored: List[Long] = machines flatMap { machine =>
      reps flatMap { i =>

        val f = repo ? StoreLoad(Load(machine, time = DateTime.now().minus(i * 1000)))
        Await.result(f, 3 seconds).asInstanceOf[List[Long]]
      }
    }
    logger.info(s"stored $stored")
  }

  override def afterAll() = {
    val purge = repo ? DeleteLoadsBefore(DateTime.now)
    Await.result(purge, 3 seconds)
  }

  "testing repo persistence" - {

    "should store load" in {

      val store: Future[List[Long]] = (repo ? StoreLoad(Load("steam machine"))).mapTo[List[Long]]

      store map { loads =>
        loads.size should be(1)
        loads.head should be(1L)
      }
    }

    "should get the last load for a single machine" in {
      val getLoads: Future[List[Load]] = (repo ? GetLastLoadFor(machines.head)).mapTo[List[Load]]
      getLoads map { loads =>
        loads.size should be(1)
      }
    }

    "should get the last load for all machines" in {
      val getLoads: Future[List[Load]] = (repo ? GetLastLoads).mapTo[List[Load]]
      getLoads map { loads =>
        loads.size should be(machines.size)
      }
    }

    "should get a list of loads for a machine in last 10 sec" in {
      val getLoads: Future[List[Load]] = (repo ? GetLoadStartingAtFor(DateTime.now().minus(10000), machines.head)) map (_.asInstanceOf[List[Load]])
      getLoads map { loads =>
        println(loads map (_.machine) distinct)
        loads.size should be(count)
      }
    }

    "should get a list of all loads for last 10 sec" in {
      val getLoads: Future[List[Load]] = (repo ? GetLoadStartingAt(DateTime.now().minus(10000))) map (_.asInstanceOf[List[Load]])
      getLoads map { loads =>
        loads.size should be(count * machines.size)
      }
    }
  }
}
