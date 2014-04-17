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

  import Dependencies._

  val org = "com.paypal.stingray"
  val scalaVsn = "2.10.4"
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

  val docScalacOptions = Seq(
    "-groups",
    "-implicits",
    "-external-urls:" +
      s"scala=http://www.scala-lang.org/api/$scalaVsn}/," +
      s"akka=http://doc.akka.io/api/akka/$akkaVersion/," +
      "java=http://docs.oracle.com/javase/6/docs/api/," +
      // this is the only scaladoc location listed on the spray site
      "spray=http://spray.io/documentation/1.1-SNAPSHOT/api/," +
      "org.slf4j=http://www.slf4j.org/api/,"+
      // make the version here dynamic once we stop using the stingray jackson fork
      "com.fasterxml.jackson=http://fasterxml.github.io/jackson-core/javadoc/2.3.0/," +
      "com.typesafe=http://typesafehub.github.io/config/latest/api/," +
      s"org.specs2=http://etorreborre.github.io/specs2/api/SPECS2-$specs2Version/"
  )

  lazy val standardSettings = Defaults.defaultSettings ++ releaseSettings ++ Plugin.graphSettings ++ ScalastylePlugin.Settings ++ Seq(
    organization := org,
    scalaVersion := scalaVsn,
    exportJars := true,
    fork := true,
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature"),
    scalacOptions in Test ++= Seq("-Yrangepos"),
    scalacOptions in (Compile, doc) ++= docScalacOptions,
    scalacOptions in (Test, doc) ++= docScalacOptions,
    javaOptions in run ++= runArgs,
    javaOptions in jacoco.Config ++= testArgs,
    javaOptions in Test ++= testArgs,
    testOptions in Test += Tests.Argument("html", "console"),
    // Add apiURL := Some(url(...)) once the scaladocs start being published
    autoAPIMappings := true,
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

  val slf4jVersion = "1.7.7"
  val fasterXmlJacksonVersion = "2.3.1-STINGRAY" //custom version until our fixes are released
  val sprayVersion = "1.3.1"
  val akkaVersion = "2.3.2"
  val parboiledVersion = "1.1.6"
  val specs2Version = "2.3.11"

  lazy val logback             = "ch.qos.logback"               % "logback-classic"             % "1.1.2" exclude("org.slf4j", "slf4j-api")

  lazy val jacksonDataBind     = "com.fasterxml.jackson.core"   % "jackson-databind"            % fasterXmlJacksonVersion exclude("com.fasterxml.jackson.core", "jackson-annotations")
  lazy val jacksonScalaModule  = "com.fasterxml.jackson.module" %% "jackson-module-scala"       % fasterXmlJacksonVersion exclude("com.fasterxml.jackson.core", "jackson-databind")

  lazy val slf4j               = "org.slf4j"                    % "slf4j-api"                   % slf4jVersion
  lazy val slf4jJul            = "org.slf4j"                    % "jul-to-slf4j"                % slf4jVersion
  lazy val slf4jJcl            = "org.slf4j"                    % "jcl-over-slf4j"              % slf4jVersion      % "runtime"
  lazy val slf4jLog4j          = "org.slf4j"                    % "log4j-over-slf4j"            % slf4jVersion      % "runtime"

  lazy val sprayCan            = "io.spray"                     % "spray-can"                   % sprayVersion
  lazy val sprayRouting        = "io.spray"                     % "spray-routing"               % sprayVersion
  lazy val akka                = "com.typesafe.akka"            %% "akka-actor"                 % akkaVersion

  lazy val specs2              = "org.specs2"                   %% "specs2"                     % specs2Version     % "test"
  lazy val scalacheck          = "org.scalacheck"               %% "scalacheck"                 % "1.11.3"          % "test"
  lazy val mockito             = "org.mockito"                  % "mockito-all"                 % "1.9.5"           % "test"
  lazy val hamcrest            = "org.hamcrest"                 % "hamcrest-all"                % "1.3"             % "test"
  lazy val pegdown             = "org.pegdown"                  % "pegdown"                     % "1.2.1"           % "test" exclude("org.parboiled", "parboiled-core") exclude("org.parboiled", "parboiled-java")
  lazy val parboiledJava       = "org.parboiled"                % "parboiled-java"              % parboiledVersion  % "test"
  lazy val parboiledScala      = "org.parboiled"                %% "parboiled-scala"            % parboiledVersion  % "test"

  lazy val sprayTestKit        = "io.spray"                     % "spray-testkit"               % sprayVersion      % "test" exclude("com.typesafe.akka", "akka-testkit_2.10")
  lazy val akkaTestKit         = "com.typesafe.akka"            %% "akka-testkit"               % akkaVersion       % "test"

  lazy val commonDependencies = Seq(
    slf4j,
    slf4jJul,
    slf4jJcl,
    slf4jLog4j,
    logback
  )

  lazy val jsonDependencies = Seq(
    jacksonDataBind,
    jacksonScalaModule
  )

  lazy val akkaDependencies = Seq(
    akka
  )

  lazy val httpDependencies = Seq(
    sprayCan,
    sprayRouting
  )

  lazy val commonTestDependencies = Seq(
    scalacheck,
    mockito,
    hamcrest,
    pegdown,
    parboiledJava,
    parboiledScala,
    specs2
  )

  lazy val akkaTestDependencies = Seq(
    akkaTestKit
  )

  lazy val httpTestDependencies = Seq(
    sprayTestKit
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
    aggregate = Seq(common, examples, json, akka, http)
  )

  lazy val common = Project("stingray-common", file("common"),
    settings = standardSettings ++ Seq(jacoco.settings: _*) ++ Seq(
      name := "stingray-common",
      libraryDependencies ++= commonDependencies ++ commonTestDependencies,
      publishArtifact in Test := true
    )
  )

  lazy val json = Project("stingray-json", file("json"),
    dependencies = Seq(common % "compile->compile;test->test"),
    settings = standardSettings ++ Seq(jacoco.settings: _*) ++ Seq(
      name := "stingray-json",
      libraryDependencies ++= jsonDependencies,
      publishArtifact in Test := true
    )
  )

  lazy val akka = Project("stingray-akka", file("akka"),
    dependencies = Seq(common % "compile->compile;test->test"),
    settings = standardSettings ++ Seq(jacoco.settings: _*) ++ Seq(
      name := "stingray-akka",
      libraryDependencies ++= akkaDependencies ++ akkaTestDependencies,
      publishArtifact in Test := true
    )
  )

  lazy val http = Project("stingray-http", file("http"),
    dependencies = Seq(
      common % "compile->compile;test->test",
      json   % "compile->compile;test->test",
      akka   % "compile->compile;test->test"
    ),
    settings = standardSettings ++ Seq(jacoco.settings: _*) ++ Seq(
      name := "stingray-http",
      libraryDependencies ++= httpDependencies ++ httpTestDependencies,
      publishArtifact in Test := true
    )
  )

  lazy val examples = Project("stingray-examples", file("examples"),
    dependencies = Seq(
      common % "compile->compile;test->test",
      json   % "compile->compile;test->test"
    ),
    settings = standardSettings ++ Seq(
      name := "stingray-examples",
      libraryDependencies ++= httpDependencies,
      publish := {}
    )
  )

}
