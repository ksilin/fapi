import sbt._

object Version {
  final val Scala     = "2.11.8"
  final val akka = "2.4.10"
  final val akkaHttpCors = "0.1.2"
  final val ScalaTest = "3.0.0"
  final val circe = "0.5.1"
  final val swaggerAkkaHttp = "0.7.2"
  final val akkaHttpCirce = "1.9.0"
  final val Logback = "1.1.3"
  final val janino = "2.6.1"
}

object Library {

  val akkaHttp = "com.typesafe.akka" %% "akka-http-experimental" % Version.akka
  val akkaHttpCirce = "de.heikoseeberger" %% "akka-http-circe" % Version.akkaHttpCirce
  val akkaHttpCors = "ch.megard" %% "akka-http-cors" % Version.akkaHttpCors
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
