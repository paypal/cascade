package com.paypal.stingray.services

import com.paypal.stingray.common.validation._
import java.io.ByteArrayInputStream
import com.paypal.stingray.common.logging.LoggingSugar
import com.paypal.stingray.common.env.EnvironmentType
import com.paypal.stingray.common.crypto._
import com.paypal.stingray.common.primitives._
import com.paypal.stingray.common.option._

class APNSSocketFactory (s3: S3FileSystem) extends LoggingSugar {

  private val logger = getLogger[APNSSocketFactory]

  def getApnsServiceForApp(appId: AppId, envType: EnvironmentType, encryptedPwd: String): Validation[Throwable, ApnsService] = {
    for {
      pwd <- decryptLegacy(encryptedPwd, appId)
      isSandbox <- (envType === EnvironmentType.DEV).success
      cert <- s3.downloadPushCertificateForApp(appId, isSandbox) toSuccess new NoCertForAppException(appId)
      apnsService <- validating {
        val input = new ByteArrayInputStream(cert)
        APNS.newService()
          .withCert(input, pwd)
          .withAppleDestination(!isSandbox)
          .build()
      } mapFailure {
        case e:AWSException => logger.error("An exception occurred while sending a message", e); e
        case e => e
      }
    } yield apnsService
  }

}


case class NoCertForAppException(appID: AppId) extends Exception("App " + appID + " doesn't have a push certificate")
