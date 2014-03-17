import de.johoop.jacoco4sbt._
import JacocoPlugin._
import net.virtualvoid.sbt.graph.Plugin
import org.scalastyle.sbt.ScalastylePlugin
import sbtrelease._
import ReleasePlugin._
import ReleaseKeys._
import sbt._
import Keys._
import com.paypal.stingray.sbt.BuildUtilities._

object BuildSettings {

  val org = "com.paypal.stingray"
  val scalaVsn = "2.10.3"
  val stingrayNexusHost = "stingray-nexus.stratus.dev.ebay.com"

  val defaultArgs = Seq(
    "-Xmx4096m",
    "-XX:MaxPermSize=512m",
    "-Xss32m",
    "-XX:ReservedCodeCacheSize=128m",
    "-XX:+UseCodeCacheFlushing",
    "-XX:+UseCompressedOops",
    "-XX:+UseConcMarkSweepGC",
    "-XX:+CMSClassUnloadingEnabled"
  )

  val runArgs = defaultArgs
  val testArgs = defaultArgs

  lazy val standardSettings = Defaults.defaultSettings ++ releaseSettings ++ Plugin.graphSettings ++ ScalastylePlugin.Settings ++ Seq(
    organization := org,
    scalaVersion := scalaVsn,
    exportJars := true,
    fork := true,
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature"),
    scalacOptions in Test ++= Seq("-Yrangepos"),
    javaOptions in run ++= runArgs,
    javaOptions in jacoco.Config ++= testArgs,
    javaOptions in Test ++= testArgs,
    testOptions in Test += Tests.Argument("html", "console"),
    publishTo <<= version { version: String =>
      val stingrayNexus = s"http://$stingrayNexusHost/nexus/content/repositories/"
      if (version.trim.endsWith("SNAPSHOT")) {
        Some("snapshots" at stingrayNexus + "snapshots/")
      } else {
        Some("releases" at stingrayNexus + "releases/")
      }
    },
    resolvers += "Stingray Nexus" at s"http://$stingrayNexusHost/nexus/content/groups/public/",
    conflictManager := ConflictManager.strict,
    dependencyOverrides <+= scalaVersion { vsn => "org.scala-lang" % "scala-library" % vsn },
    tagName <<= (version in ThisBuild).map(a => a),
    releaseProcess := defaultStingrayRelease
  )
}

object Dependencies {

  val slf4jVersion = "1.7.5"
  val fasterXmlJacksonVersion = "2.3.1-STINGRAY" //custom version until our fixes are released
  val sprayVersion = "1.3.0"
  val akkaVersion = "2.3.0"
  val parboiledVersion = "1.1.6"

  lazy val commonsCodec        = "commons-codec"                % "commons-codec"               % "1.7"
  lazy val commonsLang         = "commons-lang"                 % "commons-lang"                % "2.6"
  lazy val commonsValidator    = "commons-validator"            % "commons-validator"           % "1.4.0" exclude("commons-beanutils", "commons-beanutils")
  lazy val logback             = "ch.qos.logback"               % "logback-classic"             % "1.0.13"

  lazy val jacksonDataBind     = "com.fasterxml.jackson.core"   % "jackson-databind"            % fasterXmlJacksonVersion exclude("com.fasterxml.jackson.core", "jackson-annotations")
  lazy val jacksonScalaModule  = "com.fasterxml.jackson.module" %% "jackson-module-scala"       % fasterXmlJacksonVersion exclude("com.fasterxml.jackson.core", "jackson-databind")

  lazy val slf4j               = "org.slf4j"                    % "slf4j-api"                   % slf4jVersion
  lazy val slf4jJul            = "org.slf4j"                    % "jul-to-slf4j"                % slf4jVersion
  lazy val slf4jJcl            = "org.slf4j"                    % "jcl-over-slf4j"              % slf4jVersion      % "runtime"
  lazy val slf4jLog4j          = "org.slf4j"                    % "log4j-over-slf4j"            % slf4jVersion      % "runtime"

  lazy val sprayCan            = "io.spray"                     % "spray-can"                   % sprayVersion
  lazy val sprayRouting        = "io.spray"                     % "spray-routing"               % sprayVersion
  lazy val akka                = "com.typesafe.akka"            %% "akka-actor"                 % akkaVersion

  lazy val specs2              = "org.specs2"                   %% "specs2"                     % "2.3.8"           % "test"
  lazy val scalacheck          = "org.scalacheck"               %% "scalacheck"                 % "1.11.3"          % "test"
  lazy val mockito             = "org.mockito"                  % "mockito-all"                 % "1.9.5"           % "test"
  lazy val hamcrest            = "org.hamcrest"                 % "hamcrest-all"                % "1.3"             % "test"
  lazy val pegdown             = "org.pegdown"                  % "pegdown"                     % "1.2.1"           % "test" exclude("org.parboiled", "parboiled-core") exclude("org.parboiled", "parboiled-java")
  lazy val parboiledJava       = "org.parboiled"                % "parboiled-java"              % parboiledVersion  % "test"
  lazy val parboiledScala      = "org.parboiled"                %% "parboiled-scala"            % parboiledVersion  % "test"

  lazy val sprayTest           = "io.spray"                     % "spray-testkit"               % sprayVersion      % "test"
  lazy val akkaTestKit         = "com.typesafe.akka"            %% "akka-testkit"               % akkaVersion       % "test"

  lazy val commonDependencies = Seq(
    akka,
    slf4j,
    commonsCodec,
    commonsLang,
    commonsValidator,
    jacksonDataBind,
    jacksonScalaModule,
    slf4jJul,
    slf4jJcl,
    slf4jLog4j,
    logback
  )

  lazy val httpDependencies = Seq(
    sprayCan,
    sprayRouting,
    logback
  )

  lazy val testDependencies = Seq(
    scalacheck,
    mockito,
    hamcrest,
    pegdown,
    parboiledJava,
    parboiledScala,
    specs2,
    sprayTest,
    akkaTestKit
  )

}

object CommonBuild extends Build {
  import BuildSettings._
  import Dependencies._

  lazy val parent = Project("parent", file("."),
    settings = standardSettings ++ Seq(
      name := "parent",
      publish := {}
    ),
    aggregate = Seq(common, http)
  )

  lazy val common = Project("stingray-common", file("common"),
    settings = standardSettings ++ Seq(jacoco.settings: _*) ++ Seq(
      name := "stingray-common",
      libraryDependencies ++= commonDependencies ++ testDependencies,
      publishArtifact in Test := true
    )
  )

  lazy val http = Project("stingray-http", file("http"),
    dependencies = Seq(common % "compile->compile;test->test"),
    settings = standardSettings ++ Seq(jacoco.settings: _*) ++ Seq(
      name := "stingray-http",
      libraryDependencies ++= httpDependencies ++ testDependencies,
      publishArtifact in Test := true
    )
  )

  lazy val examples = Project("stingray-examples", file("examples"),
    dependencies = Seq(common % "compile->compile;test->test"),
    settings = standardSettings ++ Seq(
      name := "stingray-examples",
      libraryDependencies ++= httpDependencies ++ testDependencies,
      publish := {}
    )
  )

}
