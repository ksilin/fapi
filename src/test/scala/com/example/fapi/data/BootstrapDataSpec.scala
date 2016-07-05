package com.example.fapi.data

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.TestActors._
import akka.testkit.TestActors
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.example.fapi.data.sources.LoadGen.{GenLoad, Purge}
import org.scalatest.{FreeSpecLike, Matchers}
import concurrent.duration._

class BootstrapDataSpec extends TestKit(ActorSystem("bootstrapDataSpecSystem")) with FreeSpecLike with Matchers {


  "test creation of initial data" - {

    "starts load creation" in {
      BootstrapData.startGenLoad(testActor)
      expectMsgAnyOf(GenLoad, Purge)
      expectMsgAnyOf(GenLoad, Purge)
    }

    "creates initial load" in {
      BootstrapData.createLoadFor(1 minute, 50 millis)
    }

  }

}

class ForwardingActor(next: ActorRef) extends Actor {
  def receive = {
    case msg => next ! msg
  }
}
