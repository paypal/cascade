import com.paypal.stingray.sbt.BuildUtilities
import de.johoop.jacoco4sbt._
import JacocoPlugin._
import net.virtualvoid.sbt.graph.Plugin
import org.scalastyle.sbt.ScalastylePlugin
import sbtrelease._
import ReleasePlugin._
import ReleaseKeys._
import sbt._
import Keys._
import sbtunidoc.Plugin._
import sbtunidoc.Plugin.UnidocKeys._

object BuildSettings {

  import Dependencies._

  val org = "com.paypal.stingray"
  val scalaVsn = "2.11.1"
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

  val docScalacOptions = Seq("-groups", "-implicits")

  lazy val standardReleaseSettings = releaseSettings ++ Seq(
    tagName <<= (version in ThisBuild).map(a => a),
    releaseProcess := BuildUtilities.defaultReleaseProcess
  )

  lazy val standardSettings = Defaults.coreDefaultSettings ++ Plugin.graphSettings ++ ScalastylePlugin.Settings ++ Seq(
    organization := org,
    scalaVersion := scalaVsn,
    exportJars := true,
    fork := true,
    incOptions := incOptions.value.withNameHashing(true),
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature"),
    scalacOptions in Test ++= Seq("-Yrangepos"),
    scalacOptions in (Compile, doc) ++= docScalacOptions,
    scalacOptions in (Test, doc) ++= docScalacOptions,
    javaOptions in run ++= runArgs,
    javaOptions in jacoco.Config ++= testArgs,
    javaOptions in Test ++= testArgs,
    testOptions in Test += Tests.Argument("html", "console"),
    apiURL := Some(url("https://github.paypal.com/pages/Paypal-Commons-R/stingray-common/api/")),
    autoAPIMappings := true,
    apiMappings ++= {
      import BuildUtilities._
      val links = Seq(
        findManagedDependency("org.scala-lang", "scala-library").value.map(d => d -> url(s"http://www.scala-lang.org/api/$scalaVsn/")),
        findManagedDependency("com.typesafe.akka", "akka-actor").value.map(d => d -> url(s"http://doc.akka.io/api/akka/$akkaVersion/")),
        findManagedDependency("com.typesafe", "config").value.map(d => d -> url("http://typesafehub.github.io/config/latest/api/")),
        findManagedDependency("com.fasterxml.jackson.core", "jackson-core").value.map(d => d -> url(s"http://fasterxml.github.io/jackson-core/javadoc/2.4/")),
        // this is the only scaladoc location listed on the spray site
        findManagedDependency("io.spray", "spray-http").value.map(d => d -> url("http://spray.io/documentation/1.1-SNAPSHOT/api/")),
        findManagedDependency("io.spray", "spray-routing").value.map(d => d -> url("http://spray.io/documentation/1.1-SNAPSHOT/api/")),
        findManagedDependency("org.slf4j", "slf4j-api").value.map(d => d -> url("http://www.slf4j.org/api/")),
        findManagedDependency("com.typesafe.akka", "akka-testkit").value.map(d => d -> url(s"http://doc.akka.io/api/akka/$akkaVersion/")),
        findManagedDependency("org.specs2", "specs2").value.map(d => d -> url(s"http://etorreborre.github.io/specs2/api/SPECS2-$specs2Version/"))
      )
      links.collect { case Some(d) => d }.toMap
    },
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
    dependencyOverrides <++= scalaVersion { vsn => Set(
      "org.scala-lang"         %  "scala-library"  % vsn,
      "org.scala-lang"         %  "scala-compiler" % vsn,
      "org.scala-lang"         %  "scala-reflect"  % vsn,
      "org.scala-lang.modules" %% "scala-xml"      % "1.0.1"
    )}
  )

}

object Dependencies {

  val slf4jVersion = "1.7.7"
  val fasterXmlJacksonVersion = "2.4.1"
  val sprayVersion = "1.3.1"
  val akkaVersion = "2.3.3"
  val parboiledVersion = "1.1.6"
  val specs2Version = "2.3.12"

  lazy val logback             = "ch.qos.logback"                 %  "logback-classic"       % "1.1.2" exclude("org.slf4j", "slf4j-api")

  lazy val jacksonDataBind     = "com.fasterxml.jackson.core"     %  "jackson-databind"      % fasterXmlJacksonVersion exclude("com.fasterxml.jackson.core", "jackson-annotations")
  lazy val jacksonScalaModule  = "com.fasterxml.jackson.module"   %% "jackson-module-scala"  % fasterXmlJacksonVersion exclude("com.fasterxml.jackson.core", "jackson-databind")
  lazy val jacksonJodaModule   = "com.fasterxml.jackson.datatype" %  "jackson-datatype-joda" % fasterXmlJacksonVersion exclude("com.fasterxml.jackson.core", "jackson-annotations") exclude("com.fasterxml.jackson.core", "jackson-core") exclude("com.fasterxml.jackson.core", "jackson-databind")
  lazy val jodaConvert         = "org.joda"                       %  "joda-convert"          % "1.2"

  lazy val slf4j               = "org.slf4j"                      %  "slf4j-api"             % slf4jVersion
  lazy val slf4jJul            = "org.slf4j"                      %  "jul-to-slf4j"          % slf4jVersion
  lazy val slf4jJcl            = "org.slf4j"                      %  "jcl-over-slf4j"        % slf4jVersion
  lazy val slf4jLog4j          = "org.slf4j"                      %  "log4j-over-slf4j"      % slf4jVersion

  lazy val sprayCan            = "io.spray"                       %% "spray-can"             % sprayVersion
  lazy val sprayRouting        = "io.spray"                       %% "spray-routing"         % sprayVersion
  lazy val akka                = "com.typesafe.akka"              %% "akka-actor"            % akkaVersion

  lazy val specs2              = "org.specs2"                     %% "specs2"                % specs2Version     % "test"
  lazy val scalacheck          = "org.scalacheck"                 %% "scalacheck"            % "1.11.3"          % "test"
  lazy val mockito             = "org.mockito"                    %  "mockito-all"           % "1.9.5"           % "test"
  lazy val hamcrest            = "org.hamcrest"                   %  "hamcrest-all"          % "1.3"             % "test"
  lazy val pegdown             = "org.pegdown"                    %  "pegdown"               % "1.2.1"           % "test" exclude("org.parboiled", "parboiled-core") exclude("org.parboiled", "parboiled-java")
  lazy val parboiledJava       = "org.parboiled"                  %  "parboiled-java"        % parboiledVersion  % "test"
  lazy val parboiledScala      = "org.parboiled"                  %% "parboiled-scala"       % parboiledVersion  % "test"

  lazy val sprayTestKit        = "io.spray"                       %% "spray-testkit"         % sprayVersion      % "test" exclude("com.typesafe.akka", "akka-testkit_2.11")
  lazy val akkaTestKit         = "com.typesafe.akka"              %% "akka-testkit"          % akkaVersion       % "test"

  lazy val commonDependencies = Seq(
    slf4j,
    slf4jJul,
    slf4jJcl,
    slf4jLog4j,
    logback
  )

  lazy val jsonDependencies = Seq(
    jacksonDataBind,
    jacksonScalaModule,
    jacksonJodaModule,
    jodaConvert
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
    specs2,
    jacksonJodaModule,
    jodaConvert
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
    settings = standardSettings ++ BuildUtilities.utilitySettings ++ standardReleaseSettings ++ Seq(
      name := "parent",
      unidocProjectFilter in (ScalaUnidoc, unidoc) := inAnyProject -- inProjects(examples),
      publish := {}
    ),
    aggregate = Seq(common, json, akka, http, examples)
  )

  lazy val common = Project("stingray-common", file("common"),
    settings = standardSettings ++ Seq(jacoco.settings: _*) ++ Seq(
      name := "stingray-common",
      libraryDependencies ++= commonDependencies ++ commonTestDependencies,
      publishArtifact in Test := true,
      jacoco.thresholds in jacoco.Config := Thresholds(instruction = 0, method = 0, branch = 0, complexity = 0, line = 85, clazz = 0)
    )
  )

  lazy val json = Project("stingray-json", file("json"),
    dependencies = Seq(common % "compile->compile;test->test"),
    settings = standardSettings ++ Seq(jacoco.settings: _*) ++ Seq(
      name := "stingray-json",
      libraryDependencies ++= jsonDependencies,
      publishArtifact in Test := true,
      jacoco.thresholds in jacoco.Config := Thresholds(instruction = 0, method = 0, branch = 0, complexity = 0, line = 85, clazz = 0)
    )
  )

  lazy val akka = Project("stingray-akka", file("akka"),
    dependencies = Seq(common % "compile->compile;test->test"),
    settings = standardSettings ++ Seq(jacoco.settings: _*) ++ Seq(
      name := "stingray-akka",
      libraryDependencies ++= akkaDependencies ++ akkaTestDependencies,
      publishArtifact in Test := true,
      jacoco.thresholds in jacoco.Config := Thresholds(instruction = 0, method = 0, branch = 0, complexity = 0, line = 85, clazz = 0)
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
      publishArtifact in Test := true,
      jacoco.thresholds in jacoco.Config := Thresholds(instruction = 0, method = 0, branch = 0, complexity = 0, line = 85, clazz = 0)

    )
  )

  lazy val examples = Project("stingray-examples", file("examples"),
    dependencies = Seq(
      common % "compile->compile;test->test",
      json   % "compile->compile;test->test"
    ),
    settings = standardSettings ++ Seq(
      name := "stingray-examples",
      publish := {}
    )
  )

}
