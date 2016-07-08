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

package com.example.fapi

import java.time.{ Instant, LocalDateTime, ZoneOffset }
import java.util.Date

import io.getquill.naming.Literal
import io.getquill.sources.sql.idiom.H2Dialect
import io.getquill.{ JdbcSourceConfig, QueryProbing, _ }
import org.joda.time.{ DateTime, DateTimeComparator, DateTimeZone }

package object data {

  implicit val decodeDateTime = mappedEncoding[Date, DateTime](new DateTime(_))
  implicit val encodeDateTime = mappedEncoding[DateTime, Date](_.toDate)

  implicit val encodeDate = mappedEncoding[Date, LocalDateTime](d => LocalDateTime.ofInstant(Instant.ofEpochMilli(d.getTime), ZoneOffset.UTC))
  implicit val decodeDate = mappedEncoding[LocalDateTime, Date](ldt => Date.from(ldt.toInstant(ZoneOffset.UTC)))

  implicit class ForLocalDateTime(ldt: LocalDateTime) {
    def > = quote((arg: LocalDateTime) => infix"$ldt > $arg".as[Boolean])
    def < = quote((arg: LocalDateTime) => infix"$ldt < $arg".as[Boolean])
  }

  implicit class ForDateTime(ldt: DateTime) {
    def > = quote((arg: DateTime) => infix"$ldt > $arg".as[Boolean])
    def < = quote((arg: DateTime) => infix"$ldt < $arg".as[Boolean])
  }

  //  private val dbName: String = "h2DB"
  val h2DB = source(new JdbcSourceConfig[H2Dialect, Literal]("h2DB"))

  //  val h2DBWithQueryProbing = source(new JdbcSourceConfig[H2Dialect, Literal](dbName) with QueryProbing)

  case class Load(machine: String, cpu: Int = 0, mem: Int = 0, records: Long = 0L, time: DateTime = DateTime.now())

  val loads = quote {
    query[Load]
  }

  val deleteLoads = quote {
    query[Load].delete
  }

  val deleteLoadsBefore = quote { (t: DateTime) =>
    query[Load].filter(_.time < t).delete
  }

  val insertload = quote { (l: Load) =>
    query[Load].insert(l)
  }

  val loadsAfterFor = quote { (t: DateTime, machine: String) => query[Load].filter(_.time > t).filter(_.machine == machine) }

  val xlastLoadsFor = quote { (machine: String, count: Int) => query[Load].withFilter(_.machine == machine).sortBy(_.time).take(count) }

  case class Task(name: String, createdAt: DateTime = DateTime.now(), modifiedAt: Option[DateTime] = None, active: Boolean = true, id: Option[Int] = None)

  case class TaskStart(name: String, startedAt: DateTime = DateTime.now(), modifiedAt: Option[DateTime] = None, id: Option[Int] = None)
  case class TaskEnd(name: String, doneAt: DateTime = DateTime.now(), successful: Boolean = true, msg: Option[String], id: Option[Int] = None)
  case class TaskRun(name: String, createdAt: DateTime = DateTime.now(), startedAt: Option[DateTime] = None, doneAt: Option[DateTime] = None, successful: Boolean = true, msg: Option[String] = None, id: Option[Int] = None)

}
