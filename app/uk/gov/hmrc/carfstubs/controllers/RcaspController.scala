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
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import uk.gov.hmrc.carfstubs.helpers.RcaspHelper
import uk.gov.hmrc.carfstubs.models.request.CreateRCASPRequest
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.Future

class RcaspController @Inject() (cc: ControllerComponents) extends BackendController(cc) with Logging with RcaspHelper:

  def viewRcasp(carfId: String, rcaspId: String): Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"View RCASP Request received")
    val response: Result = returnRcaspResponse(carfId, rcaspId)
    logger.info(
      s"Response Code \n-> ${response.header.status}" +
        s"Response Body \n-> $response"
    )
    Future.successful(response)
  }

  def createRcasp: Action[JsValue] =
    Action.async(parse.json) { implicit request =>
      logger.info(s"Management Create Request received: \n -> ${Json.prettyPrint(request.body)}")

      request.body.validate[CreateRCASPRequest] match {
        case JsSuccess(payload, _) =>
          logger.debug("Json validation success")
          val response: Result = returnCreateResponse(payload)
          logger.info(
            s"Create Management Stub returned Response Code \n-> ${response.header.status}"
          )
          Future.successful(response)

        case JsError(errors) =>
          logger.error(s"Invalid Create Management request payload: ${errors.mkString(", ")}")
          Future.successful(BadRequest(s"Invalid Create Management payload: ${errors.mkString(", ")}"))
      }
    }
