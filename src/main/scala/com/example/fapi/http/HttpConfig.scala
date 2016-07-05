/*
 * Copyright 2016 ksilin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.fapi.http

import java.util.Map.Entry

import com.typesafe.config.{ Config, ConfigFactory, ConfigValue }
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
