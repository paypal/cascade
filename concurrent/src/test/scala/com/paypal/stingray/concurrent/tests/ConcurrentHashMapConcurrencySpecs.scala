package com.paypal.stingray.concurrent.tests

import org.specs2.Specification
import org.specs2.ScalaCheck
import org.scalacheck._
import Prop._
import com.paypal.stingray.concurrent.ConcurrentHashMap
import com.paypal.stingray.common.logging.LoggingSugar
import java.util.concurrent.CopyOnWriteArrayList
import collection.JavaConverters._

/**
 * Created by IntelliJ IDEA.
 * 
 * com.paypal.stingray.concurrent.tests
 * 
 * User: aaron
 * Date: 10/1/12
 * Time: 12:31 PM
 */

class ConcurrentHashMapConcurrencySpecs extends Specification with ScalaCheck with LoggingSugar { def is =
  """
  given an operation that's executed against a ConcurrentHashMap inside a unique thread, the resulting
  ConcurrentHashMap should be equal to the ConcurrentHashMap that's produced by serially executing all of the
  operations that have been executed previously in any thread, in the order in which they were executed
  """                                                                                                                   ! parallelOpsResultsInSameAsSerialOps ^
                                                                                                                        end
  private val key = "hello"
  private val value = "world"
  private val newValue = "worldNew"

  private type StringCHM = ConcurrentHashMap[String, String]
  private type StringMap = Map[String, String]
  private type Mutation = StringCHM => StringMap

  //a list of all of the major operations on a ConcurrentHashMap[String, String]
  private val ops = List[Mutation](
    { m =>
      m.set(key)(value)
      m.toMap
    },
    { m =>
      m.get(key)
      m.toMap
    },
    { m =>
      m.exists(key)
      m.toMap
    },
    { m =>
      m.removeIf(key){ v => v == value }
      m.toMap
    },
    { m =>
      m.set(key)(value)
      m.toMap
    },
    { m =>
      m.remove(key, value)
      m.toMap
    },
    { m =>
      m.set(key)(value)
      m.toMap
    },
    { m =>
      m.setIfPresent(key)(value)
      m.toMap
    },
    { m =>
      m.remove(key, value)
      m.toMap
    },
    { m =>
      m.setIfAbsent(key)(value)
      m.toMap
    },
    { m =>
      m.setIf(key, value)(v => v == value)
      m.toMap
    },
    { m =>
      m.replace(key, value, newValue)
      m.toMap
    },
    { m =>
      m.get(key)
      m.toMap
    },
    { m =>
      m.replace(key, newValue, value)
      m.toMap
    },
    { m =>
      m.get(key)
      m.toMap
    }
  )

  private def executeOpsInSerial(ops: Seq[Mutation]): StringMap = ops.foldLeft(Map[String, String]()){ (curMap, op) =>
    val chm = ConcurrentHashMap(curMap)
    op(chm)
  }

  private def parallelOpsResultsInSameAsSerialOps = forAll(Gen.someOf(ops)) { opsList =>
    val m = ConcurrentHashMap[String, String]()
    val opsExecuted = new CopyOnWriteArrayList[Mutation]()
    val results = opsList.map { op =>
      Promise {
        forAll(Gen.choose(0L, 2L)) { sleepLong =>
          Thread.sleep(sleepLong)
          opsExecuted.synchronized {
            val currentParallelMap = op(m)
            opsExecuted.add(op)
            currentParallelMap must beEqualTo(executeOpsInSerial(opsExecuted.subList(0, opsExecuted.size).asScala))
          }
        }
      }
    }.par.map(p => p.get)

    results.foldLeft(Prop.passed) { (running, result) =>
      result && running
    }
  }
}
