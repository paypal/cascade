package com.paypal.stingray.http

import spray.http.{MediaType, HttpHeaders, Rendering, HttpHeader}

/**
 * Created by drapp on 6/28/13.
 */
package object headers {

  trait BaseHeader extends HttpHeader {
    override def render[R <: Rendering](r: R): r.type =r ~~ name ~~ ':' ~~ ' ' ~~ value
  }

}
