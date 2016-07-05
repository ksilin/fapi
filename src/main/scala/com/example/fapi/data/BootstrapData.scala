package com.example.fapi.data

import akka.actor.{ActorRef, ActorSystem}
import com.example.fapi.data.TaskRepository.AddTask
import com.example.fapi.data.TaskRunRepository.AddTaskRun
import com.example.fapi.data.sources.LoadGen.{GenLoad, Purge}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._

object BootstrapData extends LazyLogging {

  def startGenLoad(loadGen: ActorRef)(implicit actorSystem: ActorSystem) = {
    val scheduler = actorSystem.scheduler
    implicit val executor = actorSystem.dispatcher

    scheduler.schedule(Duration.Zero, 100 millis, loadGen, GenLoad)
    scheduler.schedule(1 second, 5 minutes, loadGen, Purge)
  }

  val initTasks = List("import db1", "import db2")

  def storeInitTasks(taskRepo: ActorRef)(implicit actorSystem: ActorSystem) = {
    val scheduler = actorSystem.scheduler
    implicit val executor = actorSystem.dispatcher
    initTasks map { t => taskRepo ! AddTask(t) }
  }

  def storeInitTaskRuns(taskRunGen: ActorRef)(implicit actorSystem: ActorSystem) = {
    val scheduler = actorSystem.scheduler
    implicit val executor = actorSystem.dispatcher

    initTasks foreach { taskName =>
      taskRunGen ! AddTaskRun(taskName)
    }
  }

}
