package com.example.fapi.http

import java.util.Map.Entry

import com.typesafe.config.{Config, ConfigFactory, ConfigValue}
import scala.collection.JavaConverters._

trait HttpConfig {

  private val config = ConfigFactory.load()
  private val httpConfig = config.getConfig("http")

  val httpInterface = httpConfig.getString("interface")
  val httpPort = httpConfig.getInt("port")

  val authMap: Map[String, String] = asMap(httpConfig.getConfig("auth"))

  def asMap(config: Config) = {
    (for {
      entry: Entry[String, ConfigValue] <- config.entrySet().asScala
      key = entry.getKey
      uri = entry.getValue.unwrapped().toString
    } yield (key, uri)).toMap
  }
}