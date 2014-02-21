package com.paypal.stingray.common.json

import com.fasterxml.jackson.databind.jsontype.{TypeDeserializer}
import com.fasterxml.jackson.databind.deser
import deser.{ContextualDeserializer, Deserializers}
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.`type`.CollectionLikeType
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.module.scala.modifiers.OptionTypeModifierModule

class OptionDeserializer(elementType: JavaType, var deser: JsonDeserializer[_])
  extends JsonDeserializer[Option[AnyRef]] with ContextualDeserializer {

  override def createContextual(ctxt: DeserializationContext, property: BeanProperty): JsonDeserializer[_] = {
    val cd = ctxt.findContextualValueDeserializer(elementType, property)
    if (cd != null) new OptionDeserializer(elementType, cd)
    else this
  }

  override def deserialize(jp: JsonParser, ctxt: DeserializationContext) =
    Option(deser.deserialize(jp, ctxt)).asInstanceOf[Option[AnyRef]]

  override def getNullValue = None
}

private object OptionDeserializerResolver extends Deserializers.Base {

  val OPTION = classOf[Option[AnyRef]]

  override def findCollectionLikeDeserializer(theType: CollectionLikeType,
                                              config: DeserializationConfig,
                                              beanDesc: BeanDescription,
                                              elementTypeDeserializer: TypeDeserializer,
                                              elementDeserializer: JsonDeserializer[_]) =
    if (!OPTION.isAssignableFrom(theType.getRawClass)) {
      null
    }
    else new OptionDeserializer(theType.containedType(0), elementDeserializer)

}

trait StingrayOptionDeserializerModule extends OptionTypeModifierModule {
  this += OptionDeserializerResolver
}
