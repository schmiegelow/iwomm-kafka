import sbt.Keys.scalacOptions

lazy val settings = inThisBuild(
  List(
    organization := "com.schmiegelow",
    scalaVersion := "2.12.4",
    version := "0.1.0-SNAPSHOT"
  ))

lazy val loggingDependencies = Seq(
  "com.typesafe.akka"          %% "akka-slf4j"     % "2.5.6",
  "ch.qos.logback"             % "logback-classic" % "1.1.7",
  "com.typesafe.scala-logging" %% "scala-logging"  % "3.5.0",
  "com.github.pureconfig"      %% "pureconfig"     % "0.9.0",
)


lazy val commonDependencies = Seq(
  "com.beachape" %% "enumeratum-circe" % "1.5.14" exclude ("io.circe", "circe-core_2.12")
)

lazy val testDependencies = Seq(
  "org.scalatest"              %% "scalatest"      % "3.0.5" % "test",
  "com.madewithtea"            %% "mockedstreams"  % "1.6.0" % "test"
)

lazy val commonScalacOptions = Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-encoding",
  "utf-8", // Specify character encoding used by source files.
  "-explaintypes", // Explain type errors in more detail.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
)

lazy val converter = (project in file("converter"))
  .configs(IntegrationTest)
  .enablePlugins(JavaAppPackaging)
  .settings(
    settings,
    name := "converter",
    libraryDependencies ++= loggingDependencies ++ commonDependencies ++ testDependencies,
    scalacOptions := commonScalacOptions
  )

