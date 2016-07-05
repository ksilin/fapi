package com.example.fapi.data

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.TestActors._
import akka.testkit.TestActors
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.example.fapi.data.TaskRepository.AddTask
import com.example.fapi.data.TaskRunRepository.AddTaskRun
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

    "adds initial tasks" in {
      BootstrapData.storeInitTasks(testActor)
      val expTasks: List[AddTask] = BootstrapData.initTasks map (AddTask(_))
      expectMsgAnyOf(expTasks:_*)
      expectMsgAnyOf(expTasks:_*)
    }

    "adds runs for initial tasks" in {
      BootstrapData.storeInitTaskRuns(testActor)
      val expTasks: List[AddTaskRun] = BootstrapData.initTasks map (AddTaskRun(_))
      expectMsgAnyOf(expTasks:_*)
      expectMsgAnyOf(expTasks:_*)
    }

  }
}
