ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.5"

lazy val root = (project in file("."))
  .settings(
    name := "kafka-mini",
    idePackagePrefix := Some("io.kafka.mini"),

	libraryDependencies += "org.scala-lang" %% "toolkit" % "0.1.7"
  )
