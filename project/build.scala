import io.Source
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Calendar
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
  val stingrayNexusHost = "stingray-nexus-145194.phx-os1.stratus.dev.ebay.com:8081"

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
    javaOptions in Test ++= testArgs,
    testOptions in Test += Tests.Argument("html", "console"),
    fork := true,
    publishTo <<= version { version: String =>
      val stingrayNexus = s"$http://stingrayNexusHost/nexus/content/repositories/"
      if (version.trim.endsWith("SNAPSHOT")) {
        Some("snapshots" at stingrayNexus + "snapshots/")
      } else {
        Some("releases" at stingrayNexus + "releases/")
      }
    },
    resolvers += "Stingray Nexus" at s"$http://stingrayNexusHost/nexus/content/groups/public/",
    conflictManager := ConflictManager.strict,
    dependencyOverrides <+= scalaVersion { vsn => "org.scala-lang" % "scala-library" % vsn },
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
  val slf4jVersion = "1.7.5"
  val jacksonVersion = "1.9.9"
  val fasterXmlJacksonVersion = "2.1.2"
  val newmanVersion = "1.3.5"
  val scaliakVersion = "0.9.0"
  val sprayVersion = "1.2.0"
  val akkaVersion = "2.2.3"


  lazy val slf4j               = "org.slf4j"                 % "slf4j-api"                   % slf4jVersion
  lazy val newman              = "com.stackmob"              %% "newman"                     % newmanVersion exclude("com.twitter", "finagle-http_2.10") exclude("commons-codec", "commons-codec") exclude("org.scalaz", "scalaz-core_2.10")
  lazy val xmemcached          = "com.googlecode.xmemcached" % "xmemcached"                  % "1.4.1" exclude("org.slf4j", "slf4j-api")
  lazy val jacksonAsl          = "org.codehaus.jackson"      % "jackson-core-asl"            % jacksonVersion
  lazy val jacksonMapper       = "org.codehaus.jackson"      % "jackson-mapper-asl"          % jacksonVersion
  lazy val jacksonAnnotations  = "com.fasterxml.jackson.core"% "jackson-annotations"         % fasterXmlJacksonVersion
  lazy val jacksonCore         = "com.fasterxml.jackson.core"% "jackson-core"                % fasterXmlJacksonVersion
  lazy val commonsCodec        = "commons-codec"             % "commons-codec"               % "1.7"
  lazy val commonsLang         = "commons-lang"              % "commons-lang"                % "2.6"
  lazy val commonsValidator    = "commons-validator"         % "commons-validator"           % "1.4.0" exclude("commons-beanutils", "commons-beanutils")
  lazy val rabbitmq            = "com.rabbitmq"              % "amqp-client"                 % "2.7.1"
  lazy val logback             = "ch.qos.logback"            % "logback-classic"             % "1.0.13"
  lazy val jodaTime            = "joda-time"                 % "joda-time"                   % "2.1"
  lazy val jodaConvert         = "org.joda"                  % "joda-convert"                % "1.2" //marked as optional in joda-time
  lazy val mail                = "javax.mail"                % "mail"                        % "1.4"

  lazy val slf4jJul            = "org.slf4j"                 % "jul-to-slf4j"                % slf4jVersion
  lazy val slf4jJcl            = "org.slf4j"                 % "jcl-over-slf4j"              % slf4jVersion      % "runtime"
  lazy val slf4jLog4j          = "org.slf4j"                 % "log4j-over-slf4j"            % slf4jVersion      % "runtime"

  lazy val sprayCan            = "io.spray"                  % "spray-can"                   % sprayVersion
  lazy val sprayRouting        = "io.spray"                  % "spray-routing"               % sprayVersion
  lazy val akka                = "com.typesafe.akka"         %% "akka-actor"                 % akkaVersion

  lazy val specs2              = "org.specs2"                %% "specs2"                     % "2.2.3"           % "test" exclude("org.scalaz", "scalaz-core_2.10") exclude("org.scalaz", "scalaz-concurrent_2.10") exclude("org.scalaz", "scalaz-effect_2.10")
  lazy val scalacheck          = "org.scalacheck"            %% "scalacheck"                 % "1.10.1"          % "test"
  lazy val mockito             = "org.mockito"               % "mockito-all"                 % "1.9.0"           % "test"
  lazy val hamcrest            = "org.hamcrest"              % "hamcrest-all"                % "1.3"             % "test"
  lazy val pegdown             = "org.pegdown"               % "pegdown"                     % "1.2.1"           % "test" exclude("org.parboiled", "parboiled-core")

  lazy val newmanTest          = "com.stackmob"              %% "newman"                     % newmanVersion     % "test" classifier "tests" exclude("com.twitter", "finagle-http_2.10") exclude("commons-codec", "commons-codec") exclude("org.scalaz", "scalaz-core_2.10")
  lazy val sprayTest           = "io.spray"                  % "spray-testkit"               % sprayVersion      % "test"
  lazy val akkaTestKit         = "com.typesafe.akka"         %% "akka-testkit"               % akkaVersion       % "test"

  lazy val commonDependencies = Seq(
    slf4j,
    mail,
    xmemcached,
    commonsCodec,
    commonsLang,
    commonsValidator,
    jacksonAsl,
    jacksonMapper,
    jacksonAnnotations,
    jacksonCore,
    slf4jJul,
    slf4jJcl,
    slf4jLog4j,
    logback
  )

  lazy val serviceDependencies = Seq(
    newman,
    rabbitmq
  )

  lazy val httpDependencies = Seq(
    newman,
    sprayCan,
    sprayRouting,
    akka,
    logback,
    jodaTime,
    jodaConvert
  )

  lazy val concurrentDependencies = Seq()

  lazy val testDependencies = Seq(
    newmanTest,
    specs2,
    scalacheck,
    mockito,
    hamcrest,
    pegdown,
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
    aggregate = Seq(common, services, http, concurrent)
  )

  lazy val common = Project("stringray-common", file("common"),
    settings = standardSettings ++ Seq(
      name := "stringray-common",
      libraryDependencies ++= commonDependencies ++ testDependencies,
      publishArtifact in Test := true
    )
  )

  lazy val http = Project("stringray-http", file("http"),
    dependencies = Seq(common % "compile->compile;test->test", concurrent % "compile->compile;test->test"),
    settings = standardSettings ++ Seq(
      name := "stringray-http",
      libraryDependencies ++= httpDependencies ++ testDependencies,
      publishArtifact in Test := true
    )
  )

  lazy val services = Project("stingray-services", file("services"),
    dependencies = Seq(common % "compile->compile;test->test", concurrent % "compile->compile;test->test"),
    settings = standardSettings ++ Seq(
      name := "stringray-services",
      libraryDependencies ++= serviceDependencies ++ testDependencies,
      publishArtifact in Test := true
    )
  )


  lazy val concurrent = Project("stringray-concurrent", file("concurrent"),
    dependencies = Seq(common % "compile->compile;test->test"),
    settings = standardSettings ++ Seq(
      name := "stringray-concurrent",
      libraryDependencies ++= concurrentDependencies ++ testDependencies,
      publishArtifact in Test := true
    )
  )

}

object ChangelogReleaseStep {
  //TODO: make this a setting so it can be configured
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