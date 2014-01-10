package com.paypal.stingray.common.scaliak

import scalaz._
import scalaz.Validation._
import net.liftweb.json.scalaz.JsonScalaz._
import net.liftweb.json._
import scala.util.control.Exception._
import net.liftweb.json.JsonParser.ParseException
import com.stackmob.scaliak._
import mapping._

trait ScaliakConverterUtils {

  protected def jsonValue(obj: ReadObject, errorHandler: String => Throwable): ValidationNel[Throwable, JValue] = {
    stringValue()(obj).flatMap { jsonStr =>
      val parseResult = catching(classOf[ParseException]) either parse(jsonStr)
      fromEither(parseResult).leftMap(_ => errorHandler("error parsing json: %s".format(jsonStr))).toValidationNel
    }
  }

  protected def convertErrors[T](res: Result[T], errorHandler: String => Throwable): ValidationNel[Throwable, T] = {
    res.leftMap(_.map {
      case UnexpectedJSONError(_, _) => errorHandler("field has invalid type")
      case NoSuchFieldError(field, _) => errorHandler("field %s is missing".format(field))
      case UncategorizedError(key, desc, _) => errorHandler("could not read key (%s): %s".format(key, desc))
    })
  }

}
