package com.paypal.stingray.concurrent.tests

import org.specs2.Specification
import org.specs2.ScalaCheck
import org.scalacheck._
import org.scalacheck.Prop._
import com.paypal.stingray.concurrent.ConcurrentHashMap
import com.paypal.stingray.common.option._
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.concurrent.tests
 *
 * User: aaron
 * Date: 9/17/12
 * Time: 12:02 PM
 */
class ConcurrentHashMapSpecs extends Specification with ScalaCheck { def is =
  "get should"                                                                                                          ^
    "return None if the key doesn't exist, Some if it does"                                                             ! getReturnsCorrectly ^
                                                                                                                        end ^
  "set should"                                                                                                          ^
    "set the value regardless if it existed before"                                                                     ! setRegardlessOfExistence ^
    "return true if the value existed before, false otherwise"                                                          ! setReturnsCorrectly ^
                                                                                                                        end ^
  "exists should"                                                                                                       ^
    "return true if the key exists, false otherwise"                                                                    ! existsReturnsCorrectly ^
                                                                                                                        end ^
  "+= should"                                                                                                           ^
    "return a ConcurrentHashMap that has the new object in it"                                                          ! plusEqualsReturnsCorrectly ^
                                                                                                                        end ^
  "removeIf should"                                                                                                     ^
    "return properly and execute the predicate only if the value exists"                                                ! removeIfReturnsAndExecutesCorrectly ^
                                                                                                                        end ^
  "remove should"                                                                                                       ^
    "only remove the value if it's equal to the given one, and return true if so"                                       ! removeOnlyWorksIfValuesMatch() ^
                                                                                                                        end ^
  "-= should"                                                                                                           ^
    "return a ConcurrentHashMap without the key in it"                                                                  ! minusEqualsReturnsCorrectly ^
                                                                                                                        end ^
  "setIfPresent should"                                                                                                 ^
    "return Some and set the given value only if one already existed, None otherwise"                                   ! setIfPresentReturnsCorrectly ^
    "only evaluate the given value if one already existed"                                                              ! setIfPresentEvaluatesOnlyIfPresent ^
                                                                                                                        end ^
  "replace(key, value) should"                                                                                          ^
    "only replace if the key exists, and return true iff the key exists"                                                ! replaceOnlyWorksIfKeyExists ^
                                                                                                                        end ^
  "replace(key, oldVal, newVal) should"                                                                                 ^
    "do the same thing as setIf, except that it should always evaluate the given old and new value"                     ! replaceOnlyWorksIfKeyExistsAndIsEqual ^
                                                                                                                        end ^
  "setIfAbsent should"                                                                                                  ^
    "returns properly and execute the predicate only if the value was absent"                                           ! setIfAbsentReturnsAndExecutesCorrectly ^
                                                                                                                        end ^
  "putIfAbsent should"                                                                                                  ^
    "do the same thing as setIfAbsent, except it should always evaluate the given value"                                ! putIfAbsentReturnsCorrectly ^
                                                                                                                        end ^
  "setIf should"                                                                                                        ^
    "set, return and evaluate the predicate as documented"                                                              ! setIfReturnsAndEvaluatesCorrectly ^
                                                                                                                        end ^
  "iterator should"                                                                                                     ^
    "create an iterator that iterates over a copy of the existing ConcurrentHashMap"                                    ! iteratorCopies ^
                                                                                                                        end
  private val presentKey = "present"
  private val presentValue = "presentValue"
  private val absentKey = "absent"

  private def callByNameEvaluated[OuterRet, InnerRet](outerFn: (=> InnerRet) => OuterRet)
                                                     (innerEval: => InnerRet): (OuterRet, Boolean) = {
    val bool = new AtomicBoolean(false)
    def innerWrapped = {
      bool.set(true)
      innerEval
    }
    val result = outerFn(innerWrapped)
    (result, bool.get)
  }

  private def fnEvaluated[OuterRet, InnerParam, InnerRet](outerFn: (InnerParam => InnerRet) => OuterRet)
                                                         (innerFn: InnerParam => InnerRet): (OuterRet, Boolean) = {
    val bool = new AtomicBoolean(false)
    def innerWrapped(innerParam: InnerParam): InnerRet = {
      bool.set(true)
      innerFn(innerParam)
    }
    val outerRet = outerFn(innerWrapped _)
    (outerRet, bool.get)
  }

  private def exists[T, U](chm: ConcurrentHashMap[T, U], key: T, expected: U) = {
    chm.get(key) must beEqualTo(expected.some)
  }

  private def missing[T, U](chm: ConcurrentHashMap[T, U], key: T) = {
    chm.get(key) must beNone
  }

  type PredType[T] = T => Boolean
  def falsePred[T](t: T) = false
  def truePred[T](t: T) = true

  private def createCHM = ConcurrentHashMap(presentKey -> presentValue)

  private def getReturnsCorrectly = {
    val chm = createCHM
    exists(chm, presentKey, presentValue) and
    missing(chm, absentKey)
  }

  private def setRegardlessOfExistence() = {
    val chm = createCHM
    val setWhenPresent = {
      val newPresentValue = s"${presentValue}New"
      chm.set(presentKey)(newPresentValue)
      exists(chm, presentKey, newPresentValue)
    }
    val setWhenAbsent = {
      val newAbsentValue = s"${presentValue}New"
      chm.set(absentKey)(newAbsentValue)
      exists(chm, absentKey, newAbsentValue)
    }
    setWhenPresent and setWhenAbsent
  }

  private def setReturnsCorrectly() = {
    val chm = createCHM
    val newAbsentValue = "newAbsentValue"
    val newPresentValue = presentValue + "New"
    (chm.set(absentKey)(newAbsentValue) must beFalse) and
    (chm.set(presentKey)(newPresentValue) must beTrue) and
    (chm.get(absentKey) must beEqualTo(newAbsentValue.some)) and
    (chm.get(presentKey) must beEqualTo(newPresentValue.some))
  }

  private def existsReturnsCorrectly = {
    val chm = createCHM
    (chm.exists(presentKey) must beEqualTo(true)) and
    (chm.exists(absentKey) must beEqualTo(false))
  }

  private def plusEqualsReturnsCorrectly = {
    val chm = createCHM
    val absentBefore = chm.get(absentKey) must beNone
    val newAbsentValue = "somethingNew"
    val newCHM = chm += (absentKey -> newAbsentValue)
    val presentAfter = newCHM.get(absentKey) must beEqualTo(newAbsentValue.some)
    absentBefore and presentAfter
  }

  private def removeIfReturnsAndExecutesCorrectly() = {
    val chm = createCHM
    val (existsPredFalseOpt, existsPredFalseExecuted) = fnEvaluated(chm.removeIf(presentKey)(_: PredType[String]))(falsePred[String] _)
    val (absentPredFalseOpt, absentPredFalseExecuted) = fnEvaluated(chm.removeIf(absentKey)(_: PredType[String]))(falsePred[String] _)
    val (existsPredTrueOpt, existsPredTrueExecuted) = fnEvaluated(chm.removeIf(presentKey)(_: PredType[String]))(truePred[String] _)
    val (absentPredTrueOpt, absentPredTrueExecuted) = fnEvaluated(chm.removeIf(absentKey)(_: PredType[String]))(truePred[String] _)
    (existsPredFalseOpt must beNone) and
    (existsPredFalseExecuted must beTrue) and
    (absentPredFalseOpt must beNone) and
    (absentPredFalseExecuted must beFalse) and
    (existsPredTrueOpt must beEqualTo(presentValue.some)) and
    (existsPredTrueExecuted must beTrue) and
    (absentPredTrueOpt must beNone) and
    (absentPredTrueExecuted must beFalse)
  }

  private def removeOnlyWorksIfValuesMatch() = {
    val chm = createCHM
    val absentDoesNothing = {
      val removeResult = chm.remove(absentKey, "nothing")
      (chm.get(absentKey) must beNone) and
      (removeResult must beFalse)
    }
    val presentNoMatchDoesNothing = {
      val removeResult = chm.remove(presentKey, presentValue + "NoMatch")
      exists(chm, presentKey, presentValue) and
      (removeResult must beFalse)
    }
    val presentMatchRemoves = {
      val removeResult = chm.remove(presentKey, presentValue)
      (chm.get(presentValue) must beNone) and
      (removeResult must beTrue)
    }
    absentDoesNothing and presentNoMatchDoesNothing and presentMatchRemoves
  }

  private def minusEqualsReturnsCorrectly = {
    val chm = createCHM
    val absentDoesNothing = {
      val newCHM = chm -= absentKey
      (newCHM.exists(presentKey) must beTrue) and
      (newCHM.exists(absentKey) must beFalse)
    }
    val presentRemovesCorrectly = {
      val newCHM = chm -= presentKey
      (newCHM.exists(presentKey) must beFalse) and
      (newCHM.exists(absentKey) must beFalse)
    }

    absentDoesNothing and presentRemovesCorrectly
  }

  private def setIfPresentReturnsCorrectly() = {
    val chm = createCHM
    val absentReturnsCorrectly = chm.setIfPresent(absentKey)("absentValue") must beNone
    val presentReturnsCorrectly = chm.setIfPresent(presentKey)(presentValue + "New") must beEqualTo(presentValue.some)
    absentReturnsCorrectly and presentReturnsCorrectly
  }

  private def setIfPresentEvaluatesOnlyIfPresent() = {
    val chm = createCHM
    val absentResult = {
      val (ret, bool) = callByNameEvaluated(chm.setIfPresent(absentKey) _)(presentValue + "New")
      (ret must beNone) and
      (bool must beFalse)
    }

    val presentResult = {
      val (ret, bool) = callByNameEvaluated(chm.setIfPresent(presentKey) _)(presentValue + "New")
      (ret must beEqualTo(presentValue.some)) and
      (bool must beTrue)
    }

    absentResult and presentResult
  }

  private def replaceOnlyWorksIfKeyExists() = {
    val chm = createCHM
    val absentNoReplace = {
      (chm.replace(absentKey, "absent") must beNone) and
      (chm.exists(absentKey) must beFalse)
    }
    val presentReplaces = {
      (chm.replace(presentKey, presentValue + "New") must beEqualTo(presentValue.some)) and
      (chm.exists(presentKey) must beTrue) and
      (chm.exists(absentKey) must beFalse)
    }
    absentNoReplace and presentReplaces
  }

  private def replaceOnlyWorksIfKeyExistsAndIsEqual() = {
    val chm = createCHM
    val newPresentValue = presentValue + "New"
    val absentNoReplace = {
      (chm.replace(absentKey, "absent") must beNone) and
      (chm.exists(absentKey) must beFalse)
    }
    val presentNotEqualNoReplace = {
      (chm.replace(presentKey, presentValue + "DoesntExist", newPresentValue) must beFalse) and
      exists(chm, presentKey, presentValue)
    }
    val presentIsEqualReplaces = {
      val newPresentValue = presentValue + "New"
      (chm.replace(presentKey, presentValue, newPresentValue) must beTrue) and
      exists(chm, presentKey, newPresentValue)
    }
    absentNoReplace and presentNotEqualNoReplace and presentIsEqualReplaces
  }

  private def setIfAbsentReturnsAndExecutesCorrectly() = {
    val chm = createCHM
    val newAbsentValue = "newAbsentValue"
    val newPresentValue = presentValue + "New"
    val (absentValueOpt, absentValueEvaluated) = callByNameEvaluated(chm.setIfAbsent(absentKey) _)(newAbsentValue)
    val (presentValueOpt, presentValueEvaluated) = callByNameEvaluated(chm.setIfAbsent(presentKey) _)(newPresentValue)
    (absentValueOpt must beNone) and
    (absentValueEvaluated must beTrue) and
    (presentValueOpt must beSome(presentValue)) and
    (presentValueEvaluated must beFalse) and
    (chm.get(absentKey) must beSome(newAbsentValue)) and
    (chm.get(presentKey) must beSome(presentValue))
  }

  private def putIfAbsentReturnsCorrectly() = {
    val chm = createCHM
    val newAbsentValue = "newAbsentValue"
    (chm.putIfAbsent(absentKey, newAbsentValue) must beNone) and
    exists(chm, absentKey, newAbsentValue)
    (chm.putIfAbsent(presentKey, newAbsentValue) must beSome(presentValue)) and
    exists(chm, presentKey, presentValue)
  }

  private def setIfReturnsAndEvaluatesCorrectly() = {
    val chm = createCHM
    val newValue = "newAbsentValue"
    val (predFalseAbsentValueOpt, predFalseAbsentPredEvaluated) = fnEvaluated(chm.setIf(absentKey, newValue)(_: PredType[String]))(falsePred[String] _)
    val (predFalsePresentValueOpt, predFalsePresentValueEvaluated) = fnEvaluated(chm.setIf(presentKey, newValue)(_: PredType[String]))(falsePred[String] _)
    val (predTrueAbsentValueOpt, predTrueAbsentPredEvaluated) = fnEvaluated(chm.setIf(absentKey, newValue)(_: PredType[String]))(truePred[String] _)
    val (predTruePresentValueOpt, predTruePresentPredEvaluated) = fnEvaluated(chm.setIf(presentKey, newValue)(_: PredType[String]))(truePred[String] _)
    (predFalseAbsentValueOpt must beNone) and
    (predFalseAbsentPredEvaluated must beFalse) and
    (predFalsePresentValueOpt must beNone) and
    (predFalsePresentValueEvaluated must beTrue) and
    (predTrueAbsentValueOpt must beNone) and
    (predTrueAbsentPredEvaluated must beFalse) and
    (predTruePresentValueOpt must beSome(presentValue)) and
    (predTruePresentPredEvaluated must beTrue) and
    (exists(chm, presentKey, newValue)) and
    (missing(chm, absentKey))
  }

  private val nonEmptyAlphaStr = Gen.alphaStr.filter(_.length > 0)

  private def iteratorCopies() = {
    val chm = createCHM
    //since iterators are mutable, we can't call .toList (or other similar methods) twice with the same
    //results. since this iterator must be a copy of the map, however, we can call .toList again, and make sure the
    //resulting list has nothing in it. if a copy wasn't made, then the resulting list would have in it the
    //stuff that we just added in the forAll. pretty much every Iterator method has side effects,
    //so it's tough to think about them in a purely functional mindset
    val iter = chm.iterator
    //exhaust the iterator
    val origLength = iter.length
    val allAdditionsCreateNewCopy = forAll(nonEmptyAlphaStr, nonEmptyAlphaStr) { (key, value) =>
      chm.set(s"${key}New")(value)
      chm.iterator.toList.length must beGreaterThanOrEqualTo(origLength)
    }
    allAdditionsCreateNewCopy and
    (iter.length must beEqualTo(0))
  }
}
