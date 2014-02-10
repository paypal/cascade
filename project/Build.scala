import io.Source
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import de.johoop.jacoco4sbt._
import JacocoPlugin._
import net.virtualvoid.sbt.graph.Plugin
import org.scalastyle.sbt.ScalastylePlugin
import sbtrelease._
import ReleasePlugin._
import ReleaseKeys._
import sbt._
import Keys._
import ReleaseStateTransformations._

object BuildSettings {
  import ChangelogReleaseStep._

  val org = "com.paypal.stingray"
  val scalaVsn = "2.10.3"
  val stingrayNexusHost = "stingray-nexus.stratus.dev.ebay.com:8081"

  val propFileDir = System.getenv.get("STINGRAY_PROP_FILE_DIR")
  val defaultArgs = Seq(
    "-Xmx4096m",
    "-XX:MaxPermSize=512m",
    "-Xss32m",
    "-XX:ReservedCodeCacheSize=128m",
    "-XX:+UseCodeCacheFlushing",
    "-XX:+UseCompressedOops",
    "-XX:+UseConcMarkSweepGC",
    "-XX:+CMSClassUnloadingEnabled",
    "-Dstingray.cluster.config=%s/dev-master.properties".format(propFileDir)
  )
  val runArgs = defaultArgs ++ Seq("-Dlogback.configurationFile=%s/dev-logback.xml".format(propFileDir))
  val testArgs = defaultArgs ++ Seq("-Dlogback.configurationFile=%s/logback-test.xml".format(propFileDir))

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
    fork := true,
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
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      checkForChangelog,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      updateChangelog,
      tagRelease,
      publishArtifacts,
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )
}

object Dependencies {
  import BuildSettings.scalaVsn // to maintain consistency with above scala version

  val slf4jVersion = "1.7.5"
  val fasterXmlJacksonVersion = "2.2.2"
  val sprayVersion = "1.2.0"
  val akkaVersion = "2.2.3"
  val specsVersion = "2.3.8"
  val parboiledVersion = "1.1.6"

  lazy val commonsCodec        = "commons-codec"             % "commons-codec"               % "1.7"
  lazy val commonsLang         = "commons-lang"              % "commons-lang"                % "2.6"
  lazy val commonsValidator    = "commons-validator"         % "commons-validator"           % "1.4.0" exclude("commons-beanutils", "commons-beanutils")
  lazy val logback             = "ch.qos.logback"            % "logback-classic"             % "1.0.13"

  lazy val jacksonDataBind     = "com.fasterxml.jackson.core"   % "jackson-databind"         % fasterXmlJacksonVersion
  lazy val jacksonModule       = "com.fasterxml.jackson.module" %% "jackson-module-scala"    % fasterXmlJacksonVersion

  lazy val slf4j               = "org.slf4j"                 % "slf4j-api"                   % slf4jVersion
  lazy val slf4jJul            = "org.slf4j"                 % "jul-to-slf4j"                % slf4jVersion
  lazy val slf4jJcl            = "org.slf4j"                 % "jcl-over-slf4j"              % slf4jVersion      % "runtime"
  lazy val slf4jLog4j          = "org.slf4j"                 % "log4j-over-slf4j"            % slf4jVersion      % "runtime"

  lazy val sprayCan            = "io.spray"                  % "spray-can"                   % sprayVersion
  lazy val sprayRouting        = "io.spray"                  % "spray-routing"               % sprayVersion
  lazy val akka                = "com.typesafe.akka"         %% "akka-actor"                 % akkaVersion

  lazy val specs2Analysis      = "org.specs2"                %% "specs2-analysis"            % specsVersion      % "test"
  lazy val specs2Common        = "org.specs2"                %% "specs2-common"              % specsVersion      % "test"
  lazy val specs2Core          = "org.specs2"                %% "specs2-core"                % specsVersion      % "test"
  lazy val specs2Form          = "org.specs2"                %% "specs2-form"                % specsVersion      % "test"
  lazy val specs2Html          = "org.specs2"                %% "specs2-html"                % specsVersion      % "test"
  lazy val specs2Junit         = "org.specs2"                %% "specs2-junit"               % specsVersion      % "test"
  lazy val specs2Markdown      = "org.specs2"                %% "specs2-markdown"            % specsVersion      % "test" exclude("org.parboiled", "parboiled-core") exclude("org.parboiled", "parboiled-java")
  lazy val specs2Matcher       = "org.specs2"                %% "specs2-matcher"             % specsVersion      % "test"
  lazy val specs2MatcherExtra  = "org.specs2"                %% "specs2-matcher-extra"       % specsVersion      % "test"
  lazy val specs2Mock          = "org.specs2"                %% "specs2-mock"                % specsVersion      % "test"
  lazy val specs2Scalacheck    = "org.specs2"                %% "specs2-scalacheck"          % specsVersion      % "test"

  lazy val scalacheck          = "org.scalacheck"            %% "scalacheck"                 % "1.11.3"          % "test"
  lazy val mockito             = "org.mockito"               % "mockito-all"                 % "1.9.5"           % "test"
  lazy val hamcrest            = "org.hamcrest"              % "hamcrest-all"                % "1.3"             % "test"
  lazy val pegdown             = "org.pegdown"               % "pegdown"                     % "1.2.1"           % "test" exclude("org.parboiled", "parboiled-core") exclude("org.parboiled", "parboiled-java")
  lazy val parboiledJava       = "org.parboiled"             % "parboiled-java"              % parboiledVersion  % "test"
  lazy val parboiledScala      = "org.parboiled"             %% "parboiled-scala"            % parboiledVersion  % "test"

  lazy val sprayTest           = "io.spray"                  % "spray-testkit"               % sprayVersion      % "test"
  lazy val akkaTestKit         = "com.typesafe.akka"         %% "akka-testkit"               % akkaVersion       % "test"

  lazy val specsJars = Seq(
    specs2Analysis,
    specs2Common,
    specs2Core,
    specs2Form,
    specs2Html,
    specs2Junit,
    specs2Markdown,
    specs2Matcher,
    specs2MatcherExtra,
    specs2Mock,
    specs2Scalacheck
  )

  lazy val commonDependencies = Seq(
    akka,
    slf4j,
    commonsCodec,
    commonsLang,
    commonsValidator,
    jacksonDataBind,
    jacksonModule,
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
    sprayTest,
    akkaTestKit
  ) ++ specsJars

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

object ChangelogReleaseStep {
  val changelog = "CHANGELOG.md"

  private def getReleasedVersion(st: State) = st.get(versions).getOrElse(sys.error("No versions are set! Was this release part executed before inquireVersions?"))._1

  lazy val checkForChangelog: ReleaseStep = { st: State =>
     try {
       getChangelogInfo
       st
     } catch {
       case info: ChangelogInfoMissingException => sys.error("You must provide a changelog message and author")
       case e: Throwable => sys.error("There was an error getting the changelog info: "+ e.getMessage)
     }
   }

  lazy val updateChangelog: ReleaseStep = { st: State =>
    try {
        val info = getChangelogInfo
        updateChangelog(info, st)
        commitChangelog(st)
        st

    } catch {
      case info: ChangelogInfoMissingException => sys.error("You must provide a changelog message and author")
      case update: ChangelogUpdateException=> sys.error("There was an error writing to the changelog: " + update.getMessage)
      case commit: ChangelogCommitException => sys.error("There was an error committing the changelog: "+ commit.getMessage)
      case e: Throwable => sys.error("There was an error updating the changelog: "+ e.getMessage)
    }
  }

  case class ChangelogInfo(msg: String, author: String)

  class ChangelogInfoMissingException(e: Throwable) extends Exception(e)
  class ChangelogUpdateException(e: Throwable) extends Exception(e)
  class ChangelogCommitException(e: Throwable) extends Exception(e)

  private def getChangelogInfo: ChangelogInfo = {
    try {
      val msg = System.getProperty("changelog.msg")
      val msgExists = Option(msg).exists(_.length > 1)
      val author = System.getProperty("changelog.author")
      val authorExists = Option(author).exists(_.length > 1)
      if (msgExists & authorExists) {
        new ChangelogInfo(msg, author)
      } else {
        throw new Exception("msg or author too short")
      }
    } catch {
      case e: Throwable => throw new ChangelogInfoMissingException(e)
    }
  }

  private def updateChangelog(info: ChangelogInfo, st: State) {
    try {
      val oldChangelog = Source.fromFile(changelog).mkString
      val theVersion = getReleasedVersion(st)
      val dateFormat = new SimpleDateFormat("MM/dd/yy")
      val theDate = dateFormat.format(Calendar.getInstance().getTime)

      val out = new PrintWriter( changelog, "UTF-8")
      try {
        out.write("\n# " + theVersion + " " + theDate + " released by " + info.author + "\n")
        if (!info.msg.trim.startsWith("*")) out.write("* ")
        out.write(info.msg + "\n")
        oldChangelog.foreach(out.write(_))
      } finally {
        out.close()
      }
    } catch {
      case e: Throwable => throw new ChangelogUpdateException(e)
    }
  }

  private def commitChangelog(st: State) {
    try {
      val vcs = Project.extract(st).get(versionControlSystem).getOrElse(sys.error("Aborting release. Working directory is not a repository of a recognized VCS."))
      vcs.add(changelog) !! st.log
      vcs.commit("Changelog updated for " + getReleasedVersion(st)) ! st.log
    } catch {
      case e: Throwable  => throw new ChangelogCommitException(e)
    }
  }

}
