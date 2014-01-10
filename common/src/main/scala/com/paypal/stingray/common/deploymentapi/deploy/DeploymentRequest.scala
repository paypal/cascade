package com.paypal.stingray.common.deploymentapi.deploy

/**
 * Created by IntelliJ IDEA.
 * User: taylor
 * Date: 1/13/12
 * Time: 5:13 PM
 */

case class DeploymentRequest(userId: Long, commit: Option[String] = None, description: Option[String] = None)
