package com.example.fapi.data

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit}
import akka.util.Timeout
import akka.pattern.ask
import com.example.fapi.data.TaskRunRepository.{TaskRunStart, _}
import com.example.fapi.http.ClusterConfig
import com.typesafe.scalalogging.LazyLogging
import org.scalatest._

import scala.concurrent.duration._

class TaskRunRepositorySpec extends TestKit(ActorSystem("TaskRunRepoSpec")) with AsyncFreeSpecLike with Matchers
  with BeforeAndAfterAll with LazyLogging with ClusterConfig {

  implicit val timeout: Timeout = 10 seconds
  val repo = TestActorRef(new TaskRunRepository)
  val taskRepo = TestActorRef(new TaskRepository)

  BootstrapData.storeInitTasks(taskRepo)
  BootstrapData.storeInitTaskRuns(repo)

  "TaskRunRepository" - {

    "should return all pending tasks" in {
      val pending  = (repo ? GetPending).mapTo[List[TaskRun]]
      pending map { trs =>
        println(s"pending $trs")
        trs.size should be(1)
      }
    }

    "should return all running tasks" in {
      val running  = (repo ? GetRunning).mapTo[List[TaskRun]]
      running map { trs =>
        trs.size should be(1)
      }
    }

    "should return all finished tasks" in {
      val finished  = (repo ? GetFinished).mapTo[List[TaskRun]]
      finished map { trs =>
        trs.size should be(2)
      }
    }

    "should return all successfully finished tasks" in {
      val success  = (repo ? GetFinishedSuccessfully).mapTo[List[TaskRun]]
      success map { trs =>
        trs.size should be(1)
      }
    }
    "should return all failed tasks" in {
      val failed  = (repo ? GetFailed).mapTo[List[TaskRun]]
      failed map { trs =>
        trs.size should be(1)
      }
    }

    "should start pending task" in {

      val pending  = (repo ? GetPending).mapTo[List[TaskRun]]

      pending flatMap { (trs: List[TaskRun]) =>
        println(s"pending $trs")
        val id: Int = trs.head.id.get
        ((repo ? TaskRunStart(id)).mapTo[TaskRunUpdated]).map(r => (r, id))
      } map { case(r, id) =>
        r should be(TaskRunUpdated(id))
      }
    }

    "should ignore start if task running" in {
      val running  = (repo ? GetRunning).mapTo[List[TaskRun]]

      running flatMap { (trs: List[TaskRun]) =>
        println(s"running $trs")
        val id: Int = trs.head.id.get
        ((repo ? TaskRunStart(id)).mapTo[RunNotPending]).map(r => (r, id))
      } map { case(r, id) =>
        r should be(RunNotPending(id))
      }
    }

    "should ignore start if task finished" in {
      val finished  = (repo ? GetFinished).mapTo[List[TaskRun]]

      finished flatMap { (trs: List[TaskRun]) =>
        println(s"finished $trs")
        val id: Int = trs.head.id.get
        ((repo ? TaskRunStart(id)).mapTo[RunNotPending]).map(r => (r, id))
      } map { case(r, id) =>
        r should be(RunNotPending(id))
      }
    }

    "should successfully finish running tasks" in {
      val running  = (repo ? GetRunning).mapTo[List[TaskRun]]

      running flatMap { (trs: List[TaskRun]) =>
        println(s"running $trs")
        val id: Int = trs.head.id.get
        ((repo ? TaskRunSuccess(id)).mapTo[TaskRunUpdated]).map(r => (r, id))
      } map { case(r, id) =>
        r should be(TaskRunUpdated(id))
      }
    }

    "should ignore finish pending tasks" in {
      val pending  = (repo ? GetPending).mapTo[List[TaskRun]]

      pending flatMap { (trs: List[TaskRun]) =>
        println(s"pending $trs")
        val id: Int = trs.head.id.get
        ((repo ? TaskRunSuccess(id)).mapTo[RunNotRunning]).map(r => (r, id))
      } map { case(r, id) =>
        r should be(RunNotRunning(id))
      }
    }

    "should ignore finish finished tasks" in {
      val finished  = (repo ? GetFinished).mapTo[List[TaskRun]]

      finished flatMap { (trs: List[TaskRun]) =>
        println(s"finished $trs")
        val id: Int = trs.head.id.get
        ((repo ? TaskRunSuccess(id)).mapTo[RunNotRunning]).map(r => (r, id))
      } map { case(r, id) =>
        r should be(RunNotRunning(id))
      }
    }

  }

}
