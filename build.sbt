name := "jenkins-scala-dsl"

version := "0.0.1"

scalaVersion := "2.11.7"

scalacOptions ++= Seq("-deprecation", "-unchecked")

libraryDependencies ++= {
  Seq(
    "org.scalaz" %% "scalaz-core" % "7.1.0-RC2",
    "com.typesafe" % "config" % "1.2.1",
    "com.typesafe.akka" % "akka-actor_2.11" % "2.4.1",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
    "org.slf4j" % "slf4j-simple" % "1.7.13",
    "net.databinder.dispatch" %% "dispatch-core" % "0.11.1",
    "net.liftweb" %% "lift-json" % "2.6-M4",
    "com.sun.mail" % "javax.mail" % "1.5.2"
  )
}

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
