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

import com.example.data.{ Load, Task }
import io.circe.Decoder.Result
import io.circe.{ Decoder, Encoder, HCursor, Json }
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import org.joda.time.DateTime

package object fapi {

  type Traversable[+A] = scala.collection.immutable.Traversable[A]
  type Iterable[+A] = scala.collection.immutable.Iterable[A]
  type Seq[+A] = scala.collection.immutable.Seq[A]
  type IndexedSeq[+A] = scala.collection.immutable.IndexedSeq[A]

  import io.circe.syntax._
  implicit val dateTimeEncoder: Encoder[DateTime] = Encoder.instance(a => a.getMillis.asJson)
  implicit val dateTimeDecoder: Decoder[DateTime] = Decoder.instance(a => a.as[Long].map(new DateTime(_)))

  implicit val loadEncoder: Encoder[Load] = Encoder.instance { load =>
    Json.obj(
      "machine" -> load.machine.asJson,
      "time" -> load.time.asJson,
      "cpu" -> load.cpu.asJson,
      "mem" -> load.mem.asJson,
      "records" -> load.records.asJson
    )
  }

  implicit val taskEncoder: Encoder[Task] = Encoder.instance { task =>
    Json.obj(
      "id" -> task.id.asJson,
      "name" -> task.name.asJson,
      "active" -> task.active.asJson,
      "createdAt" -> task.createdAt.asJson,
      "modifiedAt" -> task.modifiedAt.asJson
    )
  }

  //  implicit val extractTask: Decoder[Task] = decode[Task]
  //    Decoder.instance{
  //    val cursorToResult: (HCursor) => Result[String] = _.get[String]("name")
  //  }

  //  implicit val taskDecoder: Decoder[Task] = Decoder.instance { json =>
  //    for {
  //      name <- json.downField("name").as[String]
  //      createdAt <- json.downField("createdAt").as[DateTime]
  //      updatedAt <- json.downField("updatedAt").as[Option[DateTime]]
  //      active <- json.downField("active").as[Boolean]
  //    } yield Task(name) //, createdAt, updatedAt, active)
  //  }

}
