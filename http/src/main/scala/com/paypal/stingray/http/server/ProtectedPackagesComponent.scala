package com.paypal.stingray.http.server

import java.io.File
import com.paypal.infra.protectedpkg._
import java.util.Properties
import com.paypal.stingray.common.service.ServiceNameComponent

trait ProtectedPackagesComponent {
  self: ServiceNameComponent =>

  // IMPORTANT: protected-packages requires installation of the appropriate JCE to enable longer key length size: http://www.javamex.com/tutorials/cryptography/unrestricted_policy_files.shtml
  // specific keys and properties are read and stored in global, static variables that are accessed later.
  val protectedProvider: ProtectedProvider

  val protectedPath: String // path of protected packages directory
  val cfgFilename: String // name of *_protected.cfg file in protected packages directory

  lazy val keystorePath = s"$protectedPath${File.separator}protected.jks"
}



