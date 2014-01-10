package com.paypal.stingray.common.api

import com.paypal.stingray.common.enumeration._
import com.paypal.stingray.common.env.EnvironmentType
import org.codehaus.jackson.map.annotate.{JsonDeserialize, JsonSerialize}
import org.codehaus.jackson.{JsonParser, JsonGenerator}
import org.codehaus.jackson.map._
import scalaz._
import scalaz.syntax.equal._
import scalaz.Equal._

/**
 * Created by IntelliJ IDEA.
 * User: taylor
 * Date: 3/8/12
 * Time: 3:11 PM
 */

@JsonSerialize(using = classOf[DeployStateSerializer])
@JsonDeserialize(using = classOf[DeployStateDeserializer])
sealed abstract class DeployState extends Enumeration {
  lazy val isSandbox: Boolean = this === DeployState.Sandbox
  lazy val databaseNameSuffix: String = if (isSandbox) "-sb" else ""
  lazy val envType: EnvironmentType = if (isSandbox) EnvironmentType.DEV else EnvironmentType.PROD
}

object DeployState {

  def fromBool(isSandbox: Boolean): DeployState = if (isSandbox) Sandbox else Deployed

  object Sandbox extends DeployState {
    override val stringVal = "Sandbox"
  }

  object Deployed extends DeployState {
    override val stringVal = "Deployed"
  }

  implicit val deployStateReader: EnumReader[DeployState] = new EnumReader[DeployState] {
    override def read(s: String): Option[DeployState] = s match {
      case Sandbox.stringVal => Some(Sandbox)
      case Deployed.stringVal => Some(Deployed)
      case _ => None
    }
  }

  implicit val DeployStateEqual: Equal[DeployState] = equalA

}

private[this] class DeployStateSerializer extends JsonSerializer[DeployState] {
  override def serialize(value: DeployState, jgen: JsonGenerator, provider: SerializerProvider) {
    jgen.writeString(value.stringVal)
  }
}

private[this] class DeployStateDeserializer extends JsonDeserializer[DeployState] {
  override def deserialize(jp: JsonParser, ctxt: DeserializationContext): DeployState = {
    jp.getText.toEnum[DeployState]
  }
}

