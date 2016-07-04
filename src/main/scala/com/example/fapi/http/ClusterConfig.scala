package com.example.fapi.http

import com.typesafe.config.ConfigFactory

import scala.collection.JavaConverters._

trait ClusterConfig {

  private val config = ConfigFactory.load()
  private val httpConfig = config.getConfig("cluster")

  val machines = httpConfig.getStringList("machines").asScala.to[List]
  val loadAvg = httpConfig.getStringList("loadAvg").asScala.to[List] map (_.toInt)
  val initRecords = httpConfig.getStringList("initRecords").asScala.to[List] map (_.toInt)
}