package com.example.fapi.data

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.{TestActorRef, TestKit}
import akka.util.Timeout
import com.example.fapi.data.LoadRepository._
import com.example.fapi.http.ClusterConfig
import com.typesafe.scalalogging.LazyLogging
import org.joda.time.DateTime
import org.scalatest.{AsyncFreeSpecLike, BeforeAndAfterAll, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class LoadRepositorySpec extends TestKit(ActorSystem("LoadRepoSpec")) with AsyncFreeSpecLike with Matchers
  with BeforeAndAfterAll with LazyLogging with ClusterConfig {

  implicit val timeout: Timeout = 10 seconds
  val repo = TestActorRef(new LoadRepository)

  private val count: Int = 4

  // TODO - beforeAll runs multiple times

  override def beforeAll() = {
    logger.info("-----------------------")
    logger.info(s"beforeAll running!")
    logger.info("-----------------------")
    val reps: List[Int] = (0 until count).to[List]
    val stored: List[Long] = machines flatMap { machine => reps flatMap { i =>

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

      val store: Future[List[Long]] = (repo ? StoreLoad(Load("steam machine"))) map (_.asInstanceOf[List[Long]])

      store map { loads =>
        loads.size should be(1)
        loads.head should be(1L)
      }
    }

    "should get the last load for a single machine" in {
      val getLoads: Future[List[Load]] = (repo ? GetLoadFor(machines.head)) map (_.asInstanceOf[List[Load]])
      getLoads map { loads =>
        loads.size should be(1)
      }
    }

    "should get the last load for all machines" in {
      val getLoads: Future[List[Load]] = (repo ? GetLoad) map (_.asInstanceOf[List[Load]])
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