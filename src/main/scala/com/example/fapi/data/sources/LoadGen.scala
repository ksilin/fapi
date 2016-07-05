package com.example.fapi.data.sources

import akka.actor.{Actor, ActorLogging}
import com.example.fapi.data.LoadRepository.{DeleteLoadsBefore, StoreLoad}
import com.example.fapi.data.sources.LoadGen.{GenLoad, Purge}
import com.example.fapi.data.{Load, LoadRepository}
import com.example.fapi.http.ClusterConfig
import org.joda.time.DateTime

object LoadGen {

  case object GenLoad
  case object Purge
}

class LoadGen extends Actor with ActorLogging with ClusterConfig {

  val repo = context.actorOf(LoadRepository.props())

  override def receive: Receive = {
    case GenLoad => machines foreach { machine =>
      // TODO - use machine stats to generate more plausible loads
        repo ! StoreLoad(Load(machine, 10, 10, 10000L))
    }
    case Purge => repo ! DeleteLoadsBefore(DateTime.now.minusMinutes(15))
  }
}
