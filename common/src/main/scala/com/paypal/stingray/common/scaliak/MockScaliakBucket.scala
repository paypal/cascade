package com.paypal.stingray.common.scaliak

import scalaz._
import scalaz.effect._
import Scalaz._
import com.basho.riak.client.cap.Quorum
import com.stackmob.scaliak._

/**
 * Created by IntelliJ IDEA.
 * User: jordanrw
 * Date: 1/8/12
 * Time: 8:11 PM
 */

// need a way to get around passing in all of the values
// NOTE: THIS MOCK IS NOT THREADSAFE!
class MockScaliakBucket[A](bucketName: String) extends ScaliakBucket(null, bucketName, false, false, 1,
  None, 1, 1, 1, 1, Seq(), Seq(), new Quorum(1), new Quorum(2), new Quorum(3), new Quorum(1), new Quorum(2), new Quorum(3), false, true, null, null, false) {

  case class StubbedResult(value: ValidationNel[Throwable, Option[Any]])

  var stubbedFetch: StubbedResult = StubbedResult((None: Option[Any]).successNel[Throwable])
  var stubbedPut: StubbedResult = StubbedResult((None: Option[Any]).successNel[Throwable])
  var stubbedStore: StubbedResult = StubbedResult((None: Option[Any]).successNel[Throwable])
  var stubbedBinFetchIndexByValue = StubbedResult((None: Option[List[String]]).successNel[Throwable])

  override def fetch[T](key: String,
                        r: RArgument = RArgument(),
                        pr: PRArgument = PRArgument(),
                        notFoundOk: NotFoundOkArgument = NotFoundOkArgument(),
                        basicQuorum: BasicQuorumArgument = BasicQuorumArgument(),
                        returnDeletedVClock: ReturnDeletedVCLockArgument = ReturnDeletedVCLockArgument(),
                        ifModifiedSince: IfModifiedSinceArgument = IfModifiedSinceArgument(),
                        ifModified: IfModifiedVClockArgument = IfModifiedVClockArgument())
                       (implicit converter: ScaliakConverter[T], resolver: ScaliakResolver[T]): IO[ValidationNel[Throwable, Option[T]]] = {
    (stubbedFetch.value.asInstanceOf[ValidationNel[Throwable, Option[T]]]).pure[IO]
  }

  override def put[T](obj: T,
                      w: WArgument = WArgument(),
                      pw: PWArgument = PWArgument(),
                      dw: DWArgument = DWArgument(),
                      returnBody: ReturnBodyArgument = ReturnBodyArgument())
                     (implicit converter: ScaliakConverter[T], resolver: ScaliakResolver[T]): IO[ValidationNel[Throwable, Option[T]]] = {
    (stubbedPut.value.asInstanceOf[ValidationNel[Throwable, Option[T]]]).pure[IO]
  }

  override def store[T](obj: T,
                        r: RArgument = RArgument(),
                        pr: PRArgument = PRArgument(),
                        notFoundOk: NotFoundOkArgument = NotFoundOkArgument(),
                        basicQuorum: BasicQuorumArgument = BasicQuorumArgument(),
                        returnDeletedVClock: ReturnDeletedVCLockArgument = ReturnDeletedVCLockArgument(),
                        w: WArgument = WArgument(),
                        pw: PWArgument = PWArgument(),
                        dw: DWArgument = DWArgument(),
                        returnBody: ReturnBodyArgument = ReturnBodyArgument(),
                        ifNoneMatch: Boolean = false,
                        ifNotModified: Boolean = false)
                       (implicit converter: ScaliakConverter[T], resolver: ScaliakResolver[T],
                        mutator: ScaliakMutation[T]): IO[ValidationNel[Throwable, Option[T]]] = {
    (stubbedStore.value.asInstanceOf[ValidationNel[Throwable, Option[T]]]).pure[IO]
  }

  // fake deletes by just not doing anything
  // TODO: support way to mock exceptions
  override def delete[T](obj: T, fetchBefore: Boolean = false)
                        (implicit converter: ScaliakConverter[T]): IO[Validation[Throwable, Unit]] = {
    ().success[Throwable].pure[IO]
  }

  override def deleteByKey(key: String, fetchBefore: Boolean = false): IO[Validation[Throwable, Unit]] = {
    ().success[Throwable].pure[IO]
  }

  override def fetchIndexByValue(index: String, value: String): IO[Validation[Throwable, List[String]]] = {
    // nasty hack #2, see nasty hack #1
    stubbedBinFetchIndexByValue.value.asInstanceOf[ValidationNel[Throwable, Option[List[String]]]].map(_.get).leftMap(_.head).pure[IO]
  }

  def stubFetch(value: ValidationNel[Throwable, Option[A]]) {
    stubbedFetch = StubbedResult(value)
  }

  def stubPut(value: ValidationNel[Throwable, Option[A]]) {
    stubbedPut = StubbedResult(value)
  }

  def stubStore(value: ValidationNel[Throwable, Option[A]]) {
    stubbedStore = StubbedResult(value)
  }

  def stubFetchBinIndexByValue(value: Validation[Throwable, List[String]]) {
    // nasty hack #1 because im too lazy to make StubbedResult better since this whole class is shit
    stubbedBinFetchIndexByValue = StubbedResult(value.map(Option(_)).toValidationNel)
  }

}
