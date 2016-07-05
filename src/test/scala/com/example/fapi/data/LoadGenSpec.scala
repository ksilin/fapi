package com.example.fapi.data

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit}
import akka.util.Timeout
import com.example.fapi.data.sources.LoadGen
import com.example.fapi.http.ClusterConfig
import org.scalatest.{AsyncFreeSpecLike, FreeSpec, Matchers}

import concurrent.duration._
import akka.pattern.ask
import com.example.fapi.data.LoadRepository.GetLoad
import com.example.fapi.data.sources.LoadGen.GenLoad

import scala.concurrent.Future

class LoadGenSpec extends TestKit(ActorSystem("LoadRepoSpec")) with AsyncFreeSpecLike with Matchers {

    implicit val timeout: Timeout = 10 seconds
    val repo = TestActorRef(new LoadRepository)
    val loadGen = TestActorRef(new LoadGen)

  "generating Load data"  - {

    "should gen single load per machine" in {

      val getLoad: Future[List[Load]] = (repo ? GetLoad) map (_.asInstanceOf[List[Load]])
      getLoad map { loads =>
        loads.size should be(0)
      }

      loadGen ! GenLoad

      Thread.sleep(100)

      val getLoadAfter: Future[List[Load]] = (repo ? GetLoad) map (_.asInstanceOf[List[Load]])
      getLoadAfter map { loads =>
        loads.size should be(4) // one for each machine
      }
    }
  }
}
