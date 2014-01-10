package com.paypal.stingray.common.db.mongo

object MongoConstants {

  val ID = "_id"

  // modifiers
  val MOD_SET = "$set"
  val MOD_UNSET = "$unset"
  val MOD_INC = "$inc"
  val MOD_ADD_TO_SET = "$addToSet"
  val MOD_EACH = "$each"
  val MOD_PUSH = "$push"
  val MOD_PUSH_ALL = "$pushAll"
  val MOD_PULL = "$pull"
  val MOD_PULL_ALL = "$pullAll"

  // query clauses
  val NE = "$ne"
  val LT = "$lt"
  val GT = "$gt"
  val LTE = "$lte"
  val GTE = "$gte"
  val OR = "$or"
  val AND = "$and"
  val NATURAL = "$natural"
  val REGEX = "$regex"
  val OPTIONS = "$options"
  val IN_KEY = "$in"
  val NIN_KEY = "$nin"
  val SET = "$set"

  //regex options
  val CASE_INSENSITIVE = "i"

  // geo query clauses
  val QUERY = "query"
  val MAXDISTANCE = "maxDistance"
  val GEO_NEAR = "geoNear"
  val SPHERICAL = "spherical"
  val NEAR = "near"
  val NUM = "num"
  val INCLUDELOCS = "includeLocs"
  val UNIQUEDOCS = "uniqueDocs"

  val WITHIN = "$within"
  val CENTERSPHERE = "$centerSphere"
  val BOX = "$box"
  val POLYGON = "$polygon"
  val HASHUNIQUEDOCS = "$uniqueDocs"
}
