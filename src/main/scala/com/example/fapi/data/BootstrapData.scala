package com.example.fapi.data

import akka.actor.{ActorRef, ActorSystem}
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

  def createLoadFor(duration: FiniteDuration = 5 minutes, period: FiniteDuration = 100 millis) = {

    val count: Int = (duration / period).ceil.toInt
    logger.info(s"creating $count loads for the last $duration with a period of $period ")

    // TODO - create as source
  }

}
