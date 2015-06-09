package com.paypal.cascade.http.server

import spray.http.Language
import spray.routing.ExceptionHandler

import com.paypal.cascade.http.util.HttpErrorResponeHandler

object CascadeExceptionHandler extends HttpErrorResponeHandler {
  val responseLanguage: Option[Language] = Option(Language("en", "US"))

  val handler = ExceptionHandler {
    case e: Exception => ctx => createErrorResponse(e, ctx.request, responseLanguage)
    case t: Throwable => ctx => throw t
  }
}
