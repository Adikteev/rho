import sbt._
import Keys._
import spray.revolver.RevolverPlugin._
import scala.util.Properties.envOrNone

object MyBuild extends Build {
  import Dependencies._

  lazy val rho = project
                  .in(file("."))
                  .settings(buildSettings: _*)
                  .aggregate(`rho-core`, `rho-hal`, `rho-swagger`, `rho-examples`)
   
  lazy val `rho-core` = project
                    .in(file("core"))
                    .settings(buildSettings: _*)

  lazy val `rho-hal` = project
                   .in(file("hal"))
                   .settings(buildSettings:+ halDeps : _*)
                   .dependsOn(`rho-core`)

  lazy val `rho-swagger` = project
                      .in(file("swagger"))
                      .settings(buildSettings:+ swaggerDeps : _*)
                      .dependsOn(`rho-core` % "compile->compile;test->test")

  lazy val `rho-examples` = project
                        .in(file("examples"))
                        .settings(buildSettings ++ Revolver.settings :+ exampleDeps :_*)
                        .dependsOn(`rho-swagger`, `rho-hal`)

  lazy val compileFlags = Seq("-feature")

  lazy val rhoVersion = "0.1.1"

  lazy val license = licenses in ThisBuild := Seq(
    "Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")
  )

  lazy val buildSettings = Defaults.defaultSettings ++ publishing ++
     Seq(
        scalaVersion := "2.11.2",
        scalacOptions ++= compileFlags,
        logLevel := Level.Warn,
        resolvers += Resolver.sonatypeRepo("snapshots"),
        resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
        fork in run := true,

        organization in ThisBuild := "org.http4s",
        version := rhoVersion,
        homepage in ThisBuild := Some(url("https://github.com/http4s/rho")),
        description := "A self documenting DSL build upon the http4s framework",
        license,

        libraryDependencies ++= Seq(
          http4sServer,
          logbackClassic % "test",
          scalazSpecs2 % "test"
        )
    )

  lazy val publishing = Seq(
    extras,
    credentials ++= travisCredentials.toSeq,
    publishMavenStyle in ThisBuild := true,
    publishArtifact in (ThisBuild, Test) := false,
    // Don't publish root pom.  It's not needed.
    packagedArtifacts in file(".") := Map.empty,
    publishArtifact in Test := false,
    publishTo in ThisBuild <<= version(v => Some(nexusRepoFor(v))),
    scmInfo in ThisBuild := {
      val base = "github.com/http4s/rho"
      Some(ScmInfo(url(s"https://$base"), s"scm:git:https://$base", Some(s"scm:git:git@$base")))
    }
  )

  lazy val travisCredentials = (envOrNone("SONATYPE_USERNAME"), envOrNone("SONATYPE_PASSWORD")) match {
    case (Some(user), Some(pass)) =>
      Some(Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", user, pass))
    case _ =>
      None
  }

  /** Some helper functions **************************************/
  def nexusRepoFor(version: String): Resolver = {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot(version)) "snapshots" at nexus + "content/repositories/snapshots"
    else "releases" at nexus + "service/local/staging/deploy/maven2"
  }

  def isSnapshot(version: String): Boolean = version.endsWith("-SNAPSHOT")

  lazy val extras = pomExtra in ThisBuild := (
    <developers>
      <developer>
        <id>brycelane</id>
        <name>Bryce L. Anderson</name>
        <email>bryce.anderson22@gmail.com</email>
      </developer>
      <developer>
        <id>before</id>
        <name>André Rouél</name>
      </developer>
      <developer>
        <id>rossabaker</id>
        <name>Ross A. Baker</name>
        <email>ross@rossabaker.com</email>
      </developer>
    </developers>
    )

}

object Dependencies {
  lazy val http4sVersion = "0.3.0"

  lazy val http4sServer        = "org.http4s"                 %% "http4s-server"         % http4sVersion
  lazy val http4sDSL           = "org.http4s"                 %% "http4s-dsl"            % http4sVersion
  lazy val http4sBlaze         = "org.http4s"                 %% "http4s-blazeserver"    % http4sVersion
  lazy val http4sJetty         = "org.http4s"                 %% "http4s-servlet"        % http4sVersion
  lazy val http4sJson4sJackson = "org.http4s"                 %% "http4s-json4s-jackson" % http4sVersion
  lazy val config              = "com.typesafe"                % "config"                % "1.2.1"
  lazy val json4s              = "org.json4s"                 %% "json4s-ext"            % "3.2.10"
  lazy val json4sJackson       = "org.json4s"                 %% "json4s-jackson"        % "3.2.10"
  lazy val swaggerCore         = "com.wordnik"                %% "swagger-core"          % "1.3.10"
  lazy val logbackClassic      = "ch.qos.logback"              % "logback-classic"       % "1.1.2"
  lazy val scalaloggingSlf4j   = "com.typesafe.scala-logging" %% "scala-logging-slf4j"   % "2.1.2"
  lazy val scalazSpecs2        = "org.typelevel"              %% "scalaz-specs2"         % "0.3.0"


  lazy val halDeps = libraryDependencies ++= Seq(json4sJackson)

  lazy val swaggerDeps = libraryDependencies ++= Seq(
    swaggerCore,
    json4sJackson,
    json4s
  )

  lazy val exampleDeps = libraryDependencies ++= Seq(
    http4sBlaze,
    http4sJson4sJackson
  )
}
