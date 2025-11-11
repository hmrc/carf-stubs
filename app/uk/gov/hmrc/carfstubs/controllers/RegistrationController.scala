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

import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.carfstubs.models.request.RegisterWithIDRequest
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import play.api.Logging
import play.api.libs.json.JsValue
import uk.gov.hmrc.carfstubs.helpers.RegistrationHelper

import javax.inject.Inject
import scala.concurrent.Future

class RegistrationController @Inject() (
    cc: ControllerComponents
) extends BackendController(cc)
    with Logging
    with RegistrationHelper:

  def registerIndividualWithId: Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[RegisterWithIDRequest] { request =>
      logger.info(s"%%% LOOK HERE (Stub Request) %%% \n-> $request")
      val response = returnResponse(request)
      logger.info(s"%%% LOOK HERE (Stub Response) %%% \n-> $response")
      Future.successful(response)
    }
  }

  def registerOrganisationWithId: Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[RegisterWithIDRequest] { request =>
      logger.info(s"%%% DO NOT LOOK HERE (Stub Request) %%% \n-> $request")
      val response = returnResponseOrganisation(request)
      logger.info(s"%%% LOOK HERE (Stub Response) %%% \n-> $response")
      Future.successful(response)
    }
  }
