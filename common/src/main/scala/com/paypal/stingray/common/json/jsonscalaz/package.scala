package com.paypal.stingray.common.json

import scalaz._
import scalaz.NonEmptyList._
import Scalaz._
import net.liftweb.json.scalaz.JsonScalaz._
import net.liftweb.json._
import java.util.UUID
import JsonDSL._
import java.util.Date
import java.text.SimpleDateFormat
import com.paypal.stingray.common.validation._
import java.net.URL

package object jsonscalaz {

  implicit def iterJSONR[A : JSONR]: JSONR[Iterable[A]] = new JSONR[Iterable[A]] {
    override def read(json: JValue): Result[Iterable[A]] = {
      json match {
        case JArray(xs) => xs.map(fromJSON[A]).sequence[Result, A]
        case x => UnexpectedJSONError(x, classOf[JArray]).fail.toValidationNel
      }
    }
  }

  implicit def iterJSONW[A : JSONW]: JSONW[Iterable[A]] = new JSONW[Iterable[A]] {
    override def write(values: Iterable[A]): JValue = JArray(values.map(toJSON(_)).toList)
  }

  implicit def seqJSONR[A : JSONR]: JSONR[Seq[A]] = new JSONR[Seq[A]] {
    override def read(json: JValue): Result[Seq[A]] = {
      json match {
        case JArray(xs) => xs.map(fromJSON[A]).sequence[Result, A]
        case x => UnexpectedJSONError(x, classOf[JArray]).fail.toValidationNel
      }
    }
  }

  implicit def seqJSONW[A : JSONW]: JSONW[Seq[A]] = new JSONW[Seq[A]] {
    override def write(s: Seq[A]): JValue =  JArray(s.map(toJSON(_)).toList)
  }

  implicit val uuidJSON = new JSON[UUID] {
    override def read(json: JValue): Result[UUID] = {
      json match {
        case JString(u) => validating(UUID.fromString(u)).mapFailure {
          case _: IllegalArgumentException => nel(UncategorizedError("invalid UUID", "%s was an invalid UUID".format(u), Nil), Nil)
          case e => nel(UncategorizedError(e.getClass.getName, e.getMessage, Nil), Nil)
        }
        case j => UnexpectedJSONError(j, classOf[JString]).failNel
      }
    }
    override def write(u: UUID): JValue = JString(u.toString)
  }

  implicit val dateJSON: JSON[Date] = new JSON[Date] {
    // Note: DateFormatters are not thread safe. Leave this as a def so that a new one is created at every time it's used.
    private def dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    override def read(json: JValue): Result[Date] = {
      json match {
        case JString(dateString) => validating(dateFormatter.parse(dateString)).mapFailure { _ =>
          nel(UncategorizedError("date", "%s is an invalid date".format(dateString), Nil): Error, Nil)
        }
        case s => (UnexpectedJSONError(s, classOf[JString]): Error).failNel[Date]
      }
    }
    override def write(d: Date): JValue = {
      JString(dateFormatter.format(d))
    }
  }

  implicit val urlJSON: JSON[URL] = new JSON[URL] {
    override def read(json: JValue): Result[URL] = {
      json match {
        case JString(urlString) => validating(new URL(urlString)).mapFailure { _ =>
          nel(UncategorizedError("url", "%s is an invalid URL".format(urlString), Nil): Error, Nil)
        }
        case s => (UnexpectedJSONError(s, classOf[JString]): Error).failNel[URL]
      }
    }
    override def write(url: URL): JValue = JString(url.toString)
  }

  implicit def nelJSONR[A : JSONR]: JSONR[NonEmptyList[A]] = new JSONR[NonEmptyList[A]] {
    override def read(json: JValue): Result[NonEmptyList[A]] = {
      json match {
        case JArray(xs) => {
          xs.toNel.map(_.map(fromJSON[A]).sequence[Result, A])
            .getOrElse(UncategorizedError("", "array was empty", Nil).fail[NonEmptyList[A]].toValidationNel)
        }
        case x => UnexpectedJSONError(x, classOf[JArray]).fail.toValidationNel
      }
    }
  }

  implicit def nelJSONW[A : JSONW]: JSONW[NonEmptyList[A]] = new JSONW[NonEmptyList[A]] {
    override def write(values: NonEmptyList[A]): JValue = JArray(values.list.map(toJSON(_)))
  }

  implicit val errorJSONW: JSONW[Error] = new JSONW[Error] {
    override def write(e: Error): JValue = {
      e match {
        case UnexpectedJSONError(was, expected) => ("error" -> "Unexpected JSON") ~ ("was" -> was) ~ ("expected" -> expected.toString)
        case NoSuchFieldError(name, json) => ("error" -> "No Such Field") ~ ("name" -> name) ~ ("json" -> json)
        case UncategorizedError(key, desc, args) => ("error" -> "Uncategorized") ~ ("key" -> key) ~ ("desc" -> desc) ~ ("args" -> args.map(_.toString))
      }
    }
  }

  implicit class RichError(error: Error) {
    def fold[T](unexpected: UnexpectedJSONError => T,
                noSuchField: NoSuchFieldError => T,
                uncategorized: UncategorizedError => T): T = {
      error match {
        case u@UnexpectedJSONError(_, _) => unexpected(u)
        case n@NoSuchFieldError(_, _) => noSuchField(n)
        case u@UncategorizedError(_, _, _) => uncategorized(u)
      }
    }
  }

}
