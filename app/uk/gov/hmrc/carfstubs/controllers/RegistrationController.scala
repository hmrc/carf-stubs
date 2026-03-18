/*
 * Copyright 2025 HM Revenue & Customs
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
import uk.gov.hmrc.carfstubs.helpers.RegistrationHelper
import uk.gov.hmrc.carfstubs.models.request.{RegisterWithIDRequest, RegisterWithoutIDRequestWrapper}
import uk.gov.hmrc.carfstubs.utils.JsonErrorUtils
import uk.gov.hmrc.carfstubs.models.request.RegisterWithIDApiRequest
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.Future

class RegistrationController @Inject() (
    cc: ControllerComponents
) extends BackendController(cc)
    with Logging
    with RegistrationHelper:

  def register: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[RegisterWithIDApiRequest] match {
      case JsSuccess(payload, _) =>
        logger.info(s" Stub Request Body \n-> ${Json.prettyPrint(request.body)}")
        val response = returnResponse(payload.registerWithIDRequest)
        logger.info(s" Stub Response \n-> $response")
        Future.successful(response)

      case JsError(errors) =>
        logger.error(s"Invalid RegisterWithIDApiRequest payload: $errors")
        Future.successful(BadRequest(s"Invalid RegisterWithIDApiRequest payload: $errors"))
    }
  }

  def registerWithoutId: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[RegisterWithoutIDRequestWrapper] match {
      case JsSuccess(payload, _) =>
        logger.info(s"Stub WithoutId Request Body \n-> ${Json.prettyPrint(request.body)}")
        val result: Result = returnResponseWithoutId(payload)
        logger.info(s"Stub WithoutId Response \n-> $result")
        Future.successful(result)

      case JsError(errors) =>
        val errorMsg = JsonErrorUtils.formatValidationErrors(errors)
        logger.error(s"Invalid RegisterWithoutIDRequestWrapper payload: $errorMsg")
        Future.successful(BadRequest(s"Invalid RegisterWithoutIDRequestWrapper payload: $errorMsg"))
    }
  }
