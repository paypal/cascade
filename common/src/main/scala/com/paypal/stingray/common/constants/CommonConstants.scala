package com.paypal.stingray.common.constants

object CommonConstants {
  // Original host specified in request subsequently proxied by Barney
  val FORWARDED_HOST_HEADER = "X-StackMob-Forwarded-Host"
  // Original port specified in request subsequently proxied by Barney
  val FORWARDED_PORT_HEADER = "X-StackMob-Forwarded-Port"

  val RELOAD_HEADER = "X-StackMob-Reload"
  val STATUS_CHECK_HEADER = "X-StackMob-Status"
  val HEROKU_API_HEADER = "X-StackMob-API"
  val API_KEY = "X-StackMob-API-Key"
  val HEROKU_PUSH_HEADER = "X-StackMob-Push"

  // https://developers.google.com/gdata/docs/2.0/basics#UpdatingEntry
  val METHOD_OVERRIDE_HEADER = "X-HTTP-Method-Override"

  val CC_BUILD_TAG_HEADER = "X-StackMob-CC-Build"
  val CC_FORCE_DEPLOY = "X-StackMob-Force-Deploy"

  val STACKMOB_INTERNAL_HEADER = "X-StackMob-Internal"

  lazy val PublicKeyPattern = """(?i)%s-([a-zA-Z0-9-]+)""".format(API_KEY).r

  val STACKMOB_JSON_VENDOR_MIME_TYPE = "application/vnd.stackmob+json"

  val PARAM_API_VERSION = "stackmob_api_version"
  val PARAM_API_KEY = "stackmob_api_key"


  val MDC_APP_NAME = "app_name"
  val MDC_DOMAIN_NAME = "domain_name"
  val MDC_REQUEST_ID = "request_id"
  val MDC_CLIENT_ID = "client_id"
  val MDC_APP_ID = "app_id"
  val MDC_CUSTOM_CODE = "custom_code"
  val MDC_LOG_ID = "log_id"
  val MDC_VERSION = "version"

  val APP_CTX_PREFIX = "appcontext-js-"

}
