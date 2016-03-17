import com.typesafe.sbt.SbtAspectj._

name := "akka-playground"

version := "1.0"

scalaVersion := "2.11.8"

val kamonVersion = "0.5.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.2",
  "com.typesafe" % "config" % "1.3.0",
  "io.kamon" %% "kamon-core" % kamonVersion,
  "io.kamon" %% "kamon-akka" % kamonVersion,
  "io.kamon" %% "kamon-statsd" % kamonVersion,
  "io.kamon" %% "kamon-log-reporter" % kamonVersion,
  "io.kamon" %% "kamon-system-metrics" % kamonVersion,
  "org.aspectj" % "aspectjweaver" % "1.8.8" % "compile",
  "org.specs2" %% "specs2" % "3.7" % "test"
)

aspectjSettings

javaOptions <++= AspectjKeys.weaverOptions in Aspectj
javaOptions in Test := Seq("-Dkamon.auto-start=true")

fork in run := true