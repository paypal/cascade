package com.paypal.stingray.http.server

import com.paypal.stingray.common.constants.CommonConstants
import com.paypal.stingray.common.logging.LoggingSugar
import com.paypal.stingray.common.validation._
import com.paypal.stingray.http.headers._
import scalaz._
import scalaz.NonEmptyList._
import Scalaz._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST.JValue
import StatusResponse._
import org.slf4j.LoggerFactory
import com.paypal.stingray.common.values.{StaticValues, BuildStaticValues}
import com.paypal.stingray.common.json.JSONUtil._


/**
 * Created by drapp on 6/28/13.
 */
case class StatusResponse(
                           status: String,
                           serviceName: String,
                           buildTag: String,
                           dependencies: List[String]
                           )

object StatusResponse {

  private lazy val logger = LoggerFactory.getLogger(getClass)

  private[http] val statusJSONKey = "status"
  private[http] val serviceNameJSONKey = "service-name"
  private[http] val buildTagJSONKey = "build-tag"
  private[http] val dependenciesJSONKey = "dependencies"

  implicit val statusResponseJSON = new JSON[StatusResponse] {
    override def write(statusResponse: StatusResponse): JValue = {
      (statusJSONKey -> statusResponse.status) ~
        (serviceNameJSONKey -> statusResponse.serviceName) ~
        (buildTagJSONKey -> statusResponse.buildTag) ~
        (dependenciesJSONKey -> statusResponse.dependencies)
    }
    override def read(json: JValue): Result[StatusResponse] = {
      (field[String](statusJSONKey)(json) |@|
        field[String](serviceNameJSONKey)(json) |@|
        field[String](buildTagJSONKey)(json) |@|
        field[List[String]](dependenciesJSONKey)(json)) {
        StatusResponse(_, _, _, _)
      }
    }
  }

  def getStatusResponse(svs: StaticValues, serviceName: String): StatusResponse = {
    val partialStatus = svs.get("build.tag").map { buildTag =>
      StatusResponse("ok", serviceName, buildTag, _: List[String])
    } | {
      StatusResponse("error", serviceName, "unknown", _: List[String])
    }
    val dependencies = svs.get("service.dependencies").map(_.split(",")) | Array()
    val status = partialStatus(dependencies.toList)
    logger.debug("Status Response - status: %s, build tag: %s, dependencies: %s".format(status.status, status.buildTag, dependencies.mkString(",")))
    status
  }

}

