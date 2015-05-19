/**
 * Copyright 2013-2015 PayPal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.paypal.cascade.common.properties

import java.util.Properties
import com.paypal.cascade.common.logging.LoggingSugar
import scala.util.Try

/**
 * Class specifically for accessing values from build.properties.
 *
 */
class BuildProperties extends LoggingSugar {

  /**
   * A new default properties file location, at `build.properties`, or None if no resource exists with that name
   */
  private val buildUrl = Option(getClass.getResource("/build.properties"))

  // at first use, try to retrieve a Properties object
  private lazy val props: Option[Properties] = {
    lazy val p = new Properties
    for {
      url <- buildUrl
      stream <- Try(url.openStream()).toOption
    } yield {
      p.load(stream)
      p
    }
  }

  /**
   * Retrieves an optional value from a `java.util.Properties` object
   * @param key the key to retrieve
   * @return an optional String value for the given `key`
   */
  def get(key: String): Option[String] = props.flatMap(p => Option(p.getProperty(key)))

}
