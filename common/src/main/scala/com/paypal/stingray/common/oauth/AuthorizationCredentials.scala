package com.paypal.stingray.common.oauth

import com.paypal.stingray.common.primitives._
import com.paypal.stingray.common.api.DeployState

/**
 * Created by IntelliJ IDEA.
 * User: ayakushev
 * Date: 3/7/12
 * Time: 5:26 PM
 */

case class AuthorizationCredentials(authStr: AuthorizationString, fullURL: String,
                                    verb: String, appID: AppId, deployState: DeployState)
