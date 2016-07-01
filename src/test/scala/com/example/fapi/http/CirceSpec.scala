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

import cats.data.Xor
import com.example.fapi.data.{ Load, Task }
import de.heikoseeberger.akkahttpcirce.CirceSupport
import io.circe.Decoder.Result
import org.scalatest.FreeSpec

class CirceSpec extends FreeSpec with CirceSupport {

  "circe encoding and decoding with support" - {

    //    import io.circe.Decoder.Result
    import io.circe._
    import io.circe.generic.auto._
    import io.circe.syntax._
    import io.circe.jawn._
    import io.circe.{ Decoder, Encoder, Json => JsonC }
    import io.circe.generic.semiauto._

    "simple automatic" in {

      case class Foo(bar: String, qux: Int)
      val f = Foo("lhfwdf", 13)
      val jsonF = f.asJson
      println(jsonF)
    }

    "with Datetime" in {
      val l = Load("machine")
      println(l.asJson)
    }

    "with Options" in {
      val t = Task("fake")
      val asJson: Json = t.asJson
      println(asJson)

      val taskDec: Decoder[Task] = deriveDecoder[Task]

      val jsonString: String = asJson.toString

      val parsed: Xor[ParsingFailure, JsonC] = parse(jsonString)

      val tOpt = parsed.toOption map { js => taskDec.decodeJson(js) }

      //      val t2 = parsed match {
      //        case f: Left[ParsingFailure, JsonC] => println(s"encountered parsingFailure: $f")
      //        case js: Right[ParsingFailure, JsonC] => println(s"decoded: $js")//taskDec.decodeJson(js.b)
      //      }

      println(decode[Task](asJson.spaces4))
      println(tOpt)
    }

  }

}
