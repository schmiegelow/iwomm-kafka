
scalaVersion := "2.12.3"

libraryDependencies ++= {
  val kafkaV = "1.0.1"
  Seq(
    "org.apache.kafka" % "kafka-streams" % kafkaV
  )
}

scalacOptions ++=  Seq(
  "-encoding", "UTF-8",
  "-unchecked",
)

excludeDependencies ++= {
  Seq("org.slf4j" % "slf4j-log4j12", "com.sun.jmx"  % "jmxri")
}

lazy val dockerSettings = Seq(
  packageName in Docker := "converter",
  maintainer in Docker := "Hivemind Technologies <info@hivemindtechologies.com>",
  packageSummary in Docker := "converter service",
  packageDescription := "Docker converter service",
  dockerEntrypoint := Seq("bin/converter"),
  version in Docker := version.value.split("-").head
)


