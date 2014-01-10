package com.stackmob.tests.common.json.jsonscalaz

import org.specs2.{ScalaCheck, Specification}
import org.scalacheck._
import Gen._
import Prop._
import java.net.URL
import java.util.{GregorianCalendar, Date, UUID}
import net.liftweb.json.scalaz.JsonScalaz._
import net.liftweb.json.JValue
import com.paypal.stingray.common.json.jsonscalaz._
import com.paypal.stingray.common.billing._
import com.paypal.stingray.common.deploymentapi.metadata.DeploymentHistory
import com.stackmob.tests.common.scalacheck._

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.common.tests.json.jsonscalaz
 *
 * User: aaron
 * Date: 10/17/12
 * Time: 11:32 AM
 */

class ReadWriteSpecs extends Specification with ScalaCheck with Generators { def is =
  "URL should read and write successfully"                                                                              ! roundTrip(genURL) ^
  "Date should read and write successfully"                                                                             ! roundTrip(genDate) ^
  "UUID should read and write successfully"                                                                             ! roundTrip(genUUID) ^
  "Date should read and write successfully"                                                                             ! roundTrip(genDate) ^
  "QueuePayload should read and write successfully"                                                                     ! roundTrip(genQueuePayload) ^
  "DeploymentHistory should read and write successfully"                                                                ! roundTrip(genDeploymentHistory) ^
                                                                                                                        end

  private lazy val genURL: Gen[URL] = for {
    host <- genNonEmptyAlphaStr
    domain <- Gen.value("com")
  } yield {
    new URL("http://%s.%s".format(host, domain))
  }

  private lazy val genDate: Gen[Date] = for {
    year <- posNum[Int]
    month <- chooseNum(0, 12)
    dayOfMonth <- chooseNum(1, 28)
    hourOfDay <- chooseNum(1, 24)
    minute <- chooseNum(1, 59)
    second <- chooseNum(1, 59)
  } yield {
    val cal = new GregorianCalendar(year, month, dayOfMonth, hourOfDay, minute, second)
    cal.getTime
  }

  private lazy val genUUID: Gen[UUID] = Gen.value(UUID.randomUUID())
  private lazy val genAction: Gen[Action] = Gen.oneOf(Seq[Action](Action.AddPackage, Action.RemovePackage, Action.ChangePackage))

  private lazy val genQueuePayload: Gen[QueuePayload] = for {
    fromPackageId <- genPackageId.flatMap(i => Gen.oneOf(Option(i), None))
    packageId <- genPackageId
    appId <- genAppId
    trial <- genOption(genTrial)
    action <- genAction
  } yield QueuePayload(packageId, appId, action, trial, fromPackageId)

  private lazy val genDeploymentHistory: Gen[DeploymentHistory] = for {
    appId <- posNum[Long]
    repoType <- genNonEmptyAlphaStr
    env <- genNonEmptyAlphaStr
    source <- genNonEmptyAlphaStr
    status <- genNonEmptyAlphaStr
    startTime <- genDate
    repoMetadataId <- genOption(genNonEmptyAlphaStr)
    commitSha <- genOption(genNonEmptyAlphaStr)
    repo <- genOption(genNonEmptyAlphaStr)
    org <- genOption(genNonEmptyAlphaStr)
    description <- genOption(genNonEmptyAlphaStr)
    userId <- genOption(posNum[Long])
    errorMsg <- genOption(genNonEmptyAlphaStr)
    endTime <- genOption(genDate)
    buildTag <- genOption(genNonEmptyAlphaStr)
  } yield {
    DeploymentHistory(appId,
      repoType,
      env,
      source,
      status,
      startTime,
      repoMetadataId,
      commitSha,
      repo,
      org,
      description,
      userId,
      errorMsg,
      endTime,
      buildTag)
  }

  private def roundTrip[T](gen: Gen[T])(implicit jsonr: JSONR[T], jsonw: JSONW[T]) = forAll(gen) { value: T =>
    val serialized: JValue = toJSON(value)(jsonw)
    fromJSON(serialized)(jsonr).toEither must beRight.like {
      case deserialized => deserialized must beEqualTo(value)
    }
  }

}
