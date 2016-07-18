lazy val fapi = project
  .in(file("."))
  .enablePlugins(AutomateHeaderPlugin, GitVersioning, JavaAppPackaging)

//parallelExecution in Test := false

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Vector(

  Library.scalaLogging,
  Library.logback,
  Library.janino,

  Library.akkaHttp,
  Library.akkaHttpCors,
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
