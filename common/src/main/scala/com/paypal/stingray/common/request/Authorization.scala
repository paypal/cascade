package com.paypal.stingray.common.request

import com.paypal.stingray.common.json._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import com.paypal.stingray.common.json.JSONUtil
import scala.util.{Success, Try}

/**
 * Represents the authenticated knowledge we have about the origin of an incoming request
 */
sealed abstract class Authorization {
  def getUserRefString: Option[String]

  def level: Int

  override def toString: String = "%s(%s)".format(getClass.getSimpleName, dataAsJSONString)

  protected def dataAsJSONString: String
}

object Authorization {
  def assembleUserRefString(user: String, schema: String): String = schema + "/" + user

  // The user could not be authenticated
  case class Unauthorized(e: Option[String] = None) extends Authorization {
    def getUserRefString: Option[String] = None

    def level: Int = UNAUTHORIZED_LVL

    lazy val dataAsJSONString: String = compactRender(toJSON(this))
  }
  val UNAUTHORIZED_STR = classOf[Unauthorized].getSimpleName

  private val errorJsonKey = "error"

  implicit val unauthorizedJSON: JSON[Unauthorized] = new JSON[Unauthorized] {
    def write(p: Unauthorized): JValue = (errorJsonKey -> toJSON(p.e))

    def read(json: JValue): Try[Unauthorized] =
      field[Option[String]](errorJsonKey)(json).map(Unauthorized(_))
  }

  // The user was authenticated via oauth2 users
  case class UserAuthorization(user: String, schema: String) extends Authorization {
    def getUserRefString: Option[String] = Option(assembleUserRefString(user, schema))

    def level: Int = USER_AUTH_LVL

    lazy val dataAsJSONString: String = compactRender(toJSON(this))
  }
  val USER_AUTH_STR = classOf[UserAuthorization].getSimpleName

  private val userJsonKey = "user"
  private val schemaJsonKey = "schema"

  implicit val userAuthJSON: JSON[UserAuthorization] = new JSON[UserAuthorization] {
    def write(p: UserAuthorization): JValue = (userJsonKey -> toJSON(p.user)) ~ (schemaJsonKey -> toJSON(p.schema))

    def read(json: JValue): Try[UserAuthorization] = for {
      user <- field[String](userJsonKey)(json)
      schema <- field[String](schemaJsonKey)(json)
    } yield UserAuthorization(user, schema)
  }

  // The user was authenticated via oauth1 private key
  case class ApplicationPrivateKey() extends Authorization {
    def getUserRefString: Option[String] = None

    def level: Int = PRIVATE_KEY_LVL

    val dataAsJSONString: String = ""
  }
  val PRIVATE_KEY_STR = classOf[ApplicationPrivateKey].getSimpleName

  def readString(in: String): Try[Authorization] = {
    in match {
      case readPattern(clazz, args) => {
        val result = clazz match {
          case UNAUTHORIZED_STR => fromJSON[Unauthorized](parse(args))
          case USER_AUTH_STR    => fromJSON[UserAuthorization](parse(args))
          case PRIVATE_KEY_STR  => Success(ApplicationPrivateKey())
        }
        result.recover {
          case e => Unauthorized(Some(JSONUtil.prepare(toJSON(e))))
        }
      }
      case _ => Success(Unauthorized(Some("Could not read Authorization: " + in)))
    }
  }

  val readPattern = """(\w+)\((.*)\)$""".r

  val UNAUTHORIZED_LVL = 0
  val USER_AUTH_LVL = 1
  val PRIVATE_KEY_LVL = 2
}
