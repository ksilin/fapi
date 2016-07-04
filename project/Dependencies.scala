import sbt._

object Version {
  final val Scala     = "2.11.8"
  final val akka = "2.4.7"
  final val ScalaTest = "3.0.0-RC3"
  final val circe = "0.3.0" // "0.4.1" is the latest, but 0.3.0 is required by akka-http-circe
  final val swaggerAkkaHttp = "0.7.1"
  final val akkaHttpCirce = "1.5.2"
  final val Logback = "1.1.3"
  final val janino = "2.6.1"
}

object Library {

  val akkaHttp = "com.typesafe.akka" %% "akka-http-experimental" % Version.akka
  val akkaHttpCirce = "de.heikoseeberger" %% "akka-http-circe" % Version.akkaHttpCirce
  val circe = "io.circe" %% "circe-generic" % Version.circe
  val circeParser = "io.circe" %% "circe-parser" % Version.circe
  val circeJava8 = "io.circe" %% "circe-java8" % Version.circe
  val swaggerAkkaHttp = "com.github.swagger-akka-http" %% "swagger-akka-http" % Version.swaggerAkkaHttp

  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0"
  val janino = "org.codehaus.janino" % "janino" % Version.janino
  val logback = "ch.qos.logback" % "logback-classic" % Version.Logback

  val h2connector =  "com.h2database" % "h2" % "1.4.190"
  val quillJdbc = "io.getquill" %% "quill-jdbc" % "0.7.0"

  val scalaTest = "org.scalatest" %% "scalatest" % Version.ScalaTest
  val akkaHttpTestkit = "com.typesafe.akka" %% "akka-http-testkit" % Version.akka
}
