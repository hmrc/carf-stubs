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

import play.api.libs.json.*
import play.api.mvc.*
import uk.gov.hmrc.carfstubs.helpers.SubmissionHistoryHelper
import uk.gov.hmrc.carfstubs.models.request.SubmissionHistoryRequest
import uk.gov.hmrc.carfstubs.utils.LoggerUtil.*
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.{Clock, Instant}
import javax.inject.Inject

class SubmissionHistoryController @Inject() (cc: ControllerComponents, clock: Clock)
    extends BackendController(cc)
    with SubmissionHistoryHelper {

  def getSubmissionHistory: Action[JsValue] = Action(parse.json) { implicit request =>
    logInfo(
      s"[SubmissionHistoryController][getSubmissionHistory] Submission history request received: \n ${Json.prettyPrint(request.body)}"
    )
    request.body.validate[SubmissionHistoryRequest] match {
      case JsSuccess(payload, _) =>
        logDebug("[SubmissionHistoryController][getSubmissionHistory] Json validation success")
        val response: Result = returnSubmissionHistory(
          carfId = payload.submissionsListRequest.requestDetails.subscriptionId,
          now = Instant.now(clock)
        )
        logInfo(
          s"[SubmissionHistoryController][getSubmissionHistory] Stub returned Response Code ${response.header.status}"
        )
        response

      case JsError(errors) =>
        logError(
          s"[SubmissionHistoryController][getSubmissionHistory] Invalid request payload: ${errors.mkString(", ")}"
        )
        BadRequest(s"Invalid request payload: ${errors.mkString(", ")}")
    }
  }

}
