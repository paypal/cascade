package com.paypal.stingray.common.constants

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.common.constants
 *
 * User: aaron
 * Date: 6/19/12
 * Time: 11:42 AM
 */

object PortConstants {
  val WebsitePort = 8181
  val APIServerPort = 9090
  val ManagementAPIServerPort = 9091
  val PerceptorServerPort = 9101
  val PushAPIServerPort = 9191
  val ProvisioningAPIServerPort = 9292
  val ScheduledJobServerPort = 9393
  val DeploymentAPIServerPort = 9494
  val BarneyStreamingServerPort = 9595
  val BarneyAdminServerPort = 9596
  val BarneyProxyServerPort = 9597
  val ArceeServerPort = 9599
  val IronhideServerPort = 9601
  val AllsparkServerPort = 9701
  val OptimusServerPort = 9801
  val JazzServerPort = 9839
  val YakkoPort = 9901
  val SafehouseServerPort = 9696
  val BlurrPort = 9797
  val WheeljackServerPort = 9973
  val SkywarpServerPort = 9898
  val BumblebeeServerPort = 9999
  val NemesisServerPort = 9951
  val SkidsServerPort = 9988

  //the port that clients should use to talk to Riemann. I named this specifically to include "Client" in the name
  //in case there is a RiemannServer that we ever build
  val RiemannClientPort = 5555
  val RabbitMQPort = 5672
  val MySQLPort = 3306
}
