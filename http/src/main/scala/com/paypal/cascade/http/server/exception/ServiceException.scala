/**
 * Copyright 2013-2014 PayPal
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
package com.paypal.cascade.http.server.exception

import com.paypal.cascade.common.option._

/**
 * Base type for Exceptions thrown by services. Implementations can be used to wrap other exceptions,
 * or can be thrown as exceptions themselves.
 *
 * @param message what caused this exception
 * @param throwable optionally, another exception that is caught and wrapped here
 */
abstract class ServiceException(message: String, throwable: Option[Throwable] = none[Throwable])
  extends Exception(message, throwable.orNull)
