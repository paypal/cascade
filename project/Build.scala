/**
 * Copyright 2013-2014 PayPal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.paypal.horizon.BuildUtilities
import net.virtualvoid.sbt.graph.Plugin
import org.scalastyle.sbt.ScalastylePlugin
import org.scalastyle.sbt.ScalastylePlugin._
import sbtrelease._
import ReleasePlugin._
import ReleaseKeys._
import sbt._
import Keys._
import sbtunidoc.Plugin._
import sbtunidoc.Plugin.UnidocKeys._
import scoverage.ScoverageSbtPlugin.ScoverageKeys

object BuildSettings {

  import Dependencies._

  val org = "com.paypal"
  val scalaVsn = "2.11.5"

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

  // enable correctly spaced messages for sbt 13.6+
  Option(System.getenv("CHANGELOG_MSG")).foreach { msg =>
    System.setProperty("changelog.msg", msg)
  }

  Option(System.getenv("CHANGELOG_AUTHOR")).foreach { author =>
    System.setProperty("changelog.author", author)
  }

  val runArgs = defaultArgs
  val testArgs = defaultArgs

  val docScalacOptions = Seq("-groups", "-implicits")

  lazy val standardReleaseSettings = releaseSettings ++ Seq(
    tagName <<= (version in ThisBuild).map(a => a),
    releaseProcess := BuildUtilities.signedReleaseProcess
  )

  lazy val standardSettings = Defaults.coreDefaultSettings ++ Plugin.graphSettings ++ ScalastylePlugin.projectSettings ++ Seq(
    organization := org,
    scalaVersion := scalaVsn,
    crossScalaVersions := Seq(scalaVsn, "2.10.4"),
    exportJars := true,
    fork := true,
    incOptions := incOptions.value.withNameHashing(nameHashing = true),
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-Xlint", "-target:jvm-1.7"),
    scalacOptions in Test ++= Seq("-Yrangepos"),
    scalacOptions in (Compile, doc) ++= docScalacOptions,
    scalacOptions in (Test, doc) ++= docScalacOptions,
    javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),
    javaOptions in run ++= runArgs,
    javaOptions in Test ++= testArgs,
    testOptions in Test += Tests.Argument("html", "console"),
    apiURL := Some(url("https://paypal.github.io/cascade/api/")),
    autoAPIMappings := true,
    apiMappings ++= {
      import BuildUtilities._
      val links = Seq(
        findManagedDependency("org.scala-lang", "scala-library").value.map(d => d -> url(s"http://www.scala-lang.org/api/$scalaVsn/")),
        findManagedDependency("com.typesafe.akka", "akka-actor").value.map(d => d -> url(s"http://doc.akka.io/api/akka/$akkaVersion/")),
        findManagedDependency("com.typesafe", "config").value.map(d => d -> url("http://typesafehub.github.io/config/latest/api/")),
        findManagedDependency("com.fasterxml.jackson.core", "jackson-core").value.map(d => d -> url("http://fasterxml.github.io/jackson-core/javadoc/2.4/")),
        findManagedDependency("com.fasterxml.jackson.core", "jackson-databind").value.map(d => d -> url("http://fasterxml.github.io/jackson-databind/javadoc/2.4/")),
        // this is the only scaladoc location listed on the spray site
        findManagedDependency("io.spray", "spray-http").value.map(d => d -> url("http://spray.io/documentation/1.1-SNAPSHOT/api/")),
        findManagedDependency("io.spray", "spray-routing").value.map(d => d -> url("http://spray.io/documentation/1.1-SNAPSHOT/api/")),
        findManagedDependency("org.slf4j", "slf4j-api").value.map(d => d -> url("http://www.slf4j.org/api/")),
        findManagedDependency("com.typesafe.akka", "akka-testkit").value.map(d => d -> url(s"http://doc.akka.io/api/akka/$akkaVersion/")),
        findManagedDependency("org.specs2", "specs2").value.map(d => d -> url(s"http://etorreborre.github.io/specs2/api/SPECS2-$specs2Version/"))
      )
      links.collect { case Some(d) => d }.toMap
    },
    ScoverageKeys.coverageExcludedPackages := ".*examples.*",
    publishTo := {
      val nexus = s"https://oss.sonatype.org/"
      if (isSnapshot.value) {
        Some("snapshots" at nexus + "content/repositories/snapshots")
      } else {
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
      }
    },
    conflictManager := ConflictManager.strict,
    dependencyOverrides <++= scalaVersion { vsn =>
      Set(
        "org.scala-lang"         %  "scala-library"             % vsn,
        "org.scala-lang"         %  "scala-compiler"            % vsn,
        "org.scala-lang"         %  "scala-reflect"             % vsn,
        "org.scala-lang.modules" %% "scala-xml"                 % "1.0.1",
        "org.scala-lang.modules" %% "scala-parser-combinators"  % "1.0.2"
      )
    },
    // scalaz-stream_2.10 is not on Maven Central, until that changes, this line needs to stay in
    resolvers += Resolver.bintrayRepo("scalaz", "releases"),
    scalastyleConfigUrl in Compile := Option(url("https://raw.githubusercontent.com/paypal/scala-style-guide/develop/scalastyle-config.xml")),
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    pomExtra :=
      <url>https://github.com/paypal/cascade</url>
      <licenses>
        <license>
          <name>Apache 2</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:paypal/cascade.git</url>
        <connection>scm:git:git@github.com:paypal/cascade.git</connection>
      </scm>
      <developers>
        <developer>
          <id>arschles</id>
          <name>Aaron Schlesinger</name>
          <url>https://github.com/arschles</url>
        </developer>
        <developer>
          <id>taylorleese</id>
          <name>Taylor Leese</name>
          <url>https://github.com/taylorleese</url>
        </developer>
        <developer>
          <id>ronnieftw</id>
          <name>Ronnie Chen</name>
          <url>https://github.com/ronnieftw</url>
        </developer>
      </developers>
  )
}

object Dependencies {

  val slf4jVersion = "1.7.10"
  val fasterXmlJacksonVersion = "2.4.1"
  val sprayVersion = "1.3.2"
  val akkaVersion = "2.3.9"
  val parboiledVersion = "1.1.6"
  val specs2Version = "2.4.15"

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
  lazy val scalacheck          = "org.scalacheck"                 %% "scalacheck"            % "1.12.1"          % "test"
  lazy val mockito             = "org.mockito"                    %  "mockito-all"           % "1.9.5"           % "test"
  lazy val hamcrest            = "org.hamcrest"                   %  "hamcrest-all"          % "1.3"             % "test"
  lazy val pegdown             = "org.pegdown"                    %  "pegdown"               % "1.2.1"           % "test" exclude("org.parboiled", "parboiled-core") exclude("org.parboiled", "parboiled-java")
  lazy val parboiledJava       = "org.parboiled"                  %  "parboiled-java"        % parboiledVersion  % "test"
  lazy val parboiledScala      = "org.parboiled"                  %% "parboiled-scala"       % parboiledVersion  % "test"

  lazy val sprayTestKit        = "io.spray"                       %% "spray-testkit"         % sprayVersion      % "test" exclude("org.specs2", "specs2_2.11") exclude("org.specs2", "specs2_2.10") exclude("com.typesafe.akka", "akka-testkit_2.11") exclude("com.typesafe.akka", "akka-testkit_2.10")
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
      publishArtifact := false
    ),
    aggregate = Seq(common, json, akka, http, examples)
  )

  lazy val common = Project("cascade-common", file("common"),
    settings = standardSettings ++ releaseSettings ++ Seq(
      name := "cascade-common",
      libraryDependencies ++= commonDependencies ++ commonTestDependencies,
      publishArtifact in Test := true
    )
  )

  lazy val json = Project("cascade-json", file("json"),
    dependencies = Seq(common % "compile->compile;test->test"),
    settings = standardSettings ++ releaseSettings ++ Seq(
      name := "cascade-json",
      libraryDependencies ++= jsonDependencies,
      publishArtifact in Test := true
    )
  )

  lazy val akka = Project("cascade-akka", file("akka"),
    dependencies = Seq(common % "compile->compile;test->test"),
    settings = standardSettings ++ releaseSettings ++ Seq(
      name := "cascade-akka",
      libraryDependencies ++= akkaDependencies ++ akkaTestDependencies,
      publishArtifact in Test := true
    )
  )

  lazy val http = Project("cascade-http", file("http"),
    dependencies = Seq(
      common % "compile->compile;test->test",
      json   % "compile->compile;test->test",
      akka   % "compile->compile;test->test"
    ),
    settings = standardSettings ++ releaseSettings ++ Seq(
      name := "cascade-http",
      libraryDependencies ++= httpDependencies ++ httpTestDependencies,
      publishArtifact in Test := true
    )
  )

  lazy val examples = Project("cascade-examples", file("examples"),
    dependencies = Seq(
      common % "compile->compile;test->test",
      json   % "compile->compile;test->test",
      http   % "compile->compile;test->test",
      akka   % "compile->compile;test->test"
    ),
    settings = standardSettings ++ Seq(
      name := "cascade-examples",
      publishArtifact := false
    )
  )

}
