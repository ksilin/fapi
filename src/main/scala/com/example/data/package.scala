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

package com.example

import java.util.Date

import io.getquill._
import io.getquill.naming.Literal
import io.getquill.sources.sql.idiom.H2Dialect
import org.joda.time.DateTime

package object data {

  implicit val decodeDateTime = mappedEncoding[Date, DateTime](new DateTime(_))
  implicit val encodeDateTime = mappedEncoding[DateTime, Date](_.toDate)

  private val dbName: String = "h2DB"
  val testH2DB = source(new JdbcSourceConfig[H2Dialect, Literal](dbName))
  val testH2DBWithQueryProbing = source(new JdbcSourceConfig[H2Dialect, Literal](dbName) with QueryProbing)

  case class Load(machine: String, time: DateTime = DateTime.now(), cpu: Int = 0, mem: Int = 0, records: Long = 0L)

  val loads = quote {
    query[Load]
  }

  case class Task(name: String, createdAt: DateTime = DateTime.now(), modifiedAt: Option[DateTime] = None, active: Boolean = true)

  val tasks = quote {
    query[Task]
  }

}
