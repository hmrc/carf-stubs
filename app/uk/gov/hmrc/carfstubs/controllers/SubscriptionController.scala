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
import play.api.mvc.{Action, AnyContent, ControllerComponents, Request, Result}
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
    processSubscription(false)(returnCreateResponse)
  }

  def displaySubscription(carfId: String): Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"Subscription Retrieval Request received")
    val response: Result = returnDisplayResponse(carfId)
    logger.info(
      s"Response Code \n-> ${response.header.status}" +
        s"Response Body \n-> $response"
    )
    Future.successful(response)
  }

  def updateSubscription(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    processSubscription(true)(returnUpdateResponse)
  }

  private def processSubscription(
      isUpdate: Boolean
  )(f: Subscription => Result)(implicit request: Request[JsValue]): Future[Result] = {

    val requestType     = if isUpdate then "Update" else "Create"
    val requestFunction = if isUpdate then "updateSubscription" else "createSubscription"

    logger.info(s"Subscription $requestType Request received: \n -> ${Json.prettyPrint(request.body)}")

    request.body.validate[Subscription] match {
      case JsSuccess(payload, _) =>
        logger.debug("Json validation success")
        val response: Result = f(payload)
        logger.info(
          s"$requestFunction Stub returned Response Code \n-> ${response.header.status}"
        )
        Future.successful(response)

      case JsError(errors) =>
        logger.error(s"Invalid $requestFunction payload: ${errors.mkString(", ")}")
        Future.successful(BadRequest(s"Invalid $requestFunction payload: ${errors.mkString(", ")}"))
    }
  }
