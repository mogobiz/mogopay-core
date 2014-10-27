import spray.revolver.RevolverPlugin.Revolver

import AssemblyKeys._

assemblySettings

jarName in assembly := "mogopay.jar"

mainClass in assembly := Some("mogopay.Rest")

test in assembly := {}

organization := "com.mogobiz.mogopay"

version := "0.0.1-SNAPSHOT"

logLevel in Global := Level.Warn

crossScalaVersions := Seq("2.11.1")

scalaVersion := "2.11.1"

resolvers += "BoneCP Repository" at "http://jolbox.com/bonecp/downloads/maven"

resolvers += Resolver.sonatypeRepo("releases")

resolvers += "spray repo" at "http://repo.spray.io/"

resolvers += "ebiz repo" at "http://art.ebiznext.com/artifactory/libs-release-local"

resolvers += "scribe-java-mvn-repo" at "https://raw.github.com/fernandezpablo85/scribe-java/mvn-repo"

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"


val akkaV = "2.3.3"

val sprayV = "1.3.1"


val jacksonV = "2.4.0"

val elastic4sV = "1.2.1.3"

val elasticSearchV = "1.2.1"

libraryDependencies ++= Seq(
  //"com.typesafe.slick" %% "slick" % "2.1.0",
  "joda-time" % "joda-time" % "2.3",
  "org.joda" % "joda-convert" % "1.7",
  "com.github.fernandospr" % "javapns-jdk16" % "2.2.1",
  "org.scribe" % "scribe" % "1.3.6",
  "com.typesafe" % "config" % "1.0.2",
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
  "com.jolbox" % "bonecp" % "0.8.0.RELEASE",
  "io.spray" %% "spray-can" % sprayV,
  "io.spray" %% "spray-routing" % sprayV,
  "io.spray" %% "spray-testkit" % sprayV % "test",
  "io.spray" %% "spray-client" % sprayV,
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
  "org.json4s" %% "json4s-native" % "3.2.9",
  "org.json4s" %% "json4s-jackson" % "3.2.9",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonV,
  "com.fasterxml.jackson.core" % "jackson-annotations" % jacksonV,
  "com.fasterxml.jackson.core" % "jackson-core" % jacksonV,
  "com.fasterxml.jackson.core" % "jackson-databind" % jacksonV,
  "org.specs2" %% "specs2" % "2.3.12" % "test",
  "org.apache.commons" % "commons-email" % "1.3.2",
  "org.apache.shiro" % "shiro-all" % "1.2.0",
  "com.sksamuel.elastic4s"     %% "elastic4s"                  % elastic4sV      exclude("org.elasticsearch", "elasticsearch"),
  "org.elasticsearch"           % "elasticsearch"              % elasticSearchV,
  "com.sun.xml.messaging.saaj" % "saaj-impl" % "1.3.18"
)

//fork := true

seq(Revolver.settings: _*)

mainClass in Revolver.reStart := Some("mogopay.Rest")

publishTo := {
  val artifactory = "http://art.ebiznext.com/artifactory/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at artifactory + "libs-snapshot-local")
  else
    Some("releases" at artifactory + "libs-release-local")
}

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

publishArtifact in(Compile, packageSrc) := false

publishArtifact in(Test, packageSrc) := false

parallelExecution in Test := false

packAutoSettings
