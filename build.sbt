lazy val fapi = project
  .in(file("."))
  .enablePlugins(AutomateHeaderPlugin, GitVersioning, JavaAppPackaging)

libraryDependencies ++= Vector(
  Library.akkaHttp,
  Library.akkaHttpCirce,
  Library.circe,
  Library.circeJava8,
  Library.swaggerAkkaHttp,

  Library.scalaTest % "test",
  Library.akkaHttpTestkit % "test"
)

initialCommands := """|import com.example.fapi._
                      |""".stripMargin
