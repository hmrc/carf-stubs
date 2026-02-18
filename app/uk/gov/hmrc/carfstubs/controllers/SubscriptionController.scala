/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.carfstubs.controllers

import play.api.Logging
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, ControllerComponents, Result}
import uk.gov.hmrc.carfstubs.helpers.SubscriptionHelper
import uk.gov.hmrc.carfstubs.models.request.Subscription
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.Future

class SubscriptionController @Inject() (cc: ControllerComponents)
    extends BackendController(cc)
    with Logging
    with SubscriptionHelper:

  def createSubscription: Action[JsValue] = Action.async(parse.json) { implicit request =>

    logger.info(s"Subscription Request received \n ${request.body} \n")

    request.body.validate[Subscription] match {
      case JsSuccess(payload, _) =>
        logger.info(s"createSubscription Stub Request Body \n-> ${Json.prettyPrint(request.body)}")
        val response: Result = returnResponse(payload)
        logger.info(s"createSubscription Stub Response Code \n-> ${response.header.status}")
        Future.successful(response)

      case JsError(errors) =>
        logger.error(s"Invalid createSubscription payload: $errors")
        Future.successful(BadRequest(s"Invalid createSubscription payload: $errors"))
    }
  }
