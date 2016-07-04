lazy val fapi = project
  .in(file("."))
  .enablePlugins(AutomateHeaderPlugin, GitVersioning, JavaAppPackaging)

libraryDependencies ++= Vector(

  Library.scalaLogging,
  Library.logback,
  Library.janino,

  Library.akkaHttp,
  Library.akkaHttpCirce,
  Library.circe,
  Library.circeParser,
  Library.circeJava8,
  Library.swaggerAkkaHttp,

  Library.h2connector,
  Library.quillJdbc,

  Library.scalaTest % "test",
  Library.akkaHttpTestkit % "test"
)

initialCommands := """|import com.example.fapi._
                      |""".stripMargin
