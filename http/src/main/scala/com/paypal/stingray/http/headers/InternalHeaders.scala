package com.paypal.stingray.http.headers

import spray.http.HttpMethod
import com.paypal.stingray.common.primitives._
import scalaz.NonEmptyList
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import com.paypal.stingray.common.json.JSONUtil._
import com.paypal.stingray.common.json.jsonscalaz._
import com.paypal.stingray.common.request.Authorization
import java.util.UUID
import com.paypal.stingray.common.option._

/**
 * Created by drapp on 7/2/13.
 */
object InternalHeaders {

  object `X-StackMob-Internal-Data-AppID` {
    lazy val lowercaseName = name.toLowerCase
    lazy val name = "X-StackMob-Internal-Data-AppID"
  }

  case class `X-StackMob-Internal-Data-AppID`(appId:  AppId) extends BaseHeader {
    override lazy val name = `X-StackMob-Internal-Data-AppID`.name
    override lazy val lowercaseName = `X-StackMob-Internal-Data-AppID`.lowercaseName
    override lazy val value = appId.value.toString
  }

  object `X-StackMob-Internal-Data-Modules` {
    lazy val lowercaseName = name.toLowerCase
    lazy val name = "X-StackMob-Internal-Data-Modules"

    def apply(modules: NonEmptyList[ModuleId]): `X-StackMob-Internal-Data-Modules` = apply(Some(modules))
  }

  case class `X-StackMob-Internal-Data-Modules`(modules:  Option[NonEmptyList[ModuleId]]) extends BaseHeader {
    override lazy val name = `X-StackMob-Internal-Data-Modules`.name
    override lazy val lowercaseName = `X-StackMob-Internal-Data-Modules`.lowercaseName
    override lazy val value = toJSON(modules.list.map(_.toString)).toWireFormat
  }

  object `X-StackMob-Internal-Data-API-Version` {
    lazy val lowercaseName = name.toLowerCase
    lazy val name = "X-StackMob-Internal-Data-API-Version"
  }

  case class `X-StackMob-Internal-Data-API-Version`(version:  APIVersionNumber) extends BaseHeader {
    override lazy val name = `X-StackMob-Internal-Data-API-Version`.name
    override lazy val lowercaseName = `X-StackMob-Internal-Data-API-Version`.lowercaseName
    override lazy val value = version.value.toString
  }

  object `X-StackMob-Internal-Data-Authorization` {
    lazy val lowercaseName = name.toLowerCase
    lazy val name = "X-StackMob-Internal-Data-Authorization"
  }

  case class `X-StackMob-Internal-Data-Authorization`(auth: Authorization) extends BaseHeader {
    override lazy val name = `X-StackMob-Internal-Data-Authorization`.name
    override lazy val lowercaseName = `X-StackMob-Internal-Data-Authorization`.lowercaseName
    override lazy val value = auth.toString
  }

  object `X-StackMob-Internal-Data-Request-UUID` {
    lazy val lowercaseName = name.toLowerCase
    lazy val name = "X-StackMob-Internal-Data-Request-UUID"
  }

  case class `X-StackMob-Internal-Data-Request-UUID`(uuid: UUID) extends BaseHeader {
    override lazy val name = `X-StackMob-Internal-Data-Request-UUID`.name
    override lazy val lowercaseName = `X-StackMob-Internal-Data-Request-UUID`.lowercaseName
    override lazy val value = uuid.toString
  }

  object `X-StackMob-Internal-Data-Internal` {
    lazy val lowercaseName = name.toLowerCase
    lazy val name = "X-StackMob-Internal-Data-Internal"
  }

  case class `X-StackMob-Internal-Data-Internal`(internal: Boolean) extends BaseHeader {
    override lazy val name = `X-StackMob-Internal-Data-Internal`.name
    override lazy val lowercaseName = `X-StackMob-Internal-Data-Internal`.lowercaseName
    override lazy val value = internal.toString
  }
}
