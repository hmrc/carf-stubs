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

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.carfstubs.config.AppConfig
import uk.gov.hmrc.carfstubs.models.response.FileListing
import uk.gov.hmrc.carfstubs.models.submissionCallback.BusinessRulesFileListing
import uk.gov.hmrc.carfstubs.utils.LoggerUtil.logWarn
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}

@Singleton
class FileListingController @Inject() (
    cc: ControllerComponents,
    appConfig: AppConfig
) extends BackendController(cc) {

  def getBusinessRulesFileListing(informationType: String): Action[AnyContent] = Action { implicit request =>
    (informationType, request.headers.get("x-client-id"), request.headers.get("x-sdes-key")) match {
      case (appConfig.informationType, Some(appConfig.clientId), Some(appConfig.srn)) =>
        Ok(Json.toJson(BusinessRulesFileListing.businessRulesFilesInBackend))
      case _                                                                          =>
        logWarn(
          "[FileListingController][getBusinessRulesFileListing] Request does not contain required informationType or headers"
        )
        BadRequest("Request does not contain required informationType or headers")
    }
  }

}
