package com.paypal.stingray.http.server

import java.security.{SecureRandom, KeyStore}
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}
import spray.io._
import java.io.{File, FileInputStream}

trait SslConfigurationComponent {
  self: ProtectedPackagesComponent =>

  // implicity injected for ServerSSLEngineProvider
  implicit def sslContext: SSLContext = {
    val keyStorePassword = protectedProvider.getPropertyAsString("encrypted_keystore_passphrase", "passwordstg2")

    val keyStore = KeyStore.getInstance("JKS")
    keyStore.load(new FileInputStream(keystorePath), keyStorePassword.toCharArray)
    val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(keyStore, keyStorePassword.toCharArray)
    val trustManagerFactory = TrustManagerFactory.getInstance("SunX509")
    trustManagerFactory.init(keyStore)
    val context = SSLContext.getInstance("TLS")

    context.init(keyManagerFactory.getKeyManagers, trustManagerFactory.getTrustManagers, new SecureRandom)
    context
  }

  implicit def sslEngineProvider: ServerSSLEngineProvider = {
    ServerSSLEngineProvider { engine =>
      engine.setEnabledCipherSuites(Array("TLS_RSA_WITH_AES_256_CBC_SHA"))
      engine.setEnabledProtocols(Array("SSLv3", "TLSv1"))
      engine
    }
  }
}

