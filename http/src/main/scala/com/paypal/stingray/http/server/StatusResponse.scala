package com.paypal.stingray.http.server

import org.slf4j.LoggerFactory
import com.paypal.stingray.common.properties.BuildProperties

/**
 * Container for the information generated by a `status` endpoint request
 * @param status the status
 * @param serviceName the name
 * @param dependencies the dependencies used
 */
case class StatusResponse(status: String,
                          serviceName: String,
                          dependencies: List[String])

object StatusResponse {

  private lazy val logger = LoggerFactory.getLogger(getClass)

  /**
   * Generate a StatusResponse based on current information
   * @param props the BuildProperties instance to read
   * @param serviceName the name
   * @return a StatusResponse object
   */
  def getStatusResponse(props: BuildProperties, serviceName: String): StatusResponse = {
    val dependencies = props.get("service.dependencies").map(_.split(",")).getOrElse(Array())
    val status = StatusResponse("ok", serviceName, dependencies.toList)
    logger.debug(s"Status Response - status: ${status.status}, dependencies: ${dependencies.mkString(",")}")
    status
  }

}

