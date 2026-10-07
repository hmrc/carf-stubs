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

package uk.gov.hmrc.carfstubs.helpers

import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.mvc.Results.*
import uk.gov.hmrc.carfstubs.models.response.*
import uk.gov.hmrc.carfstubs.utils.HelperUtil.errorDetailJson

import java.time.Instant

trait SubmissionHistoryHelper {

  def returnSubmissionHistory(carfId: String, now: Instant): Result =
    carfId.takeRight(2).take(1) match {
      case "9" => internalServerError500Response
      case "8" => badRequest400Response
      case "7" => serviceUnavailable503Response
      case "6" => unprocessableEntity422Response
      case "5" => notAllowedResponse
      case "4" => forbiddenResponse
      case "3" => Ok(Json.toJson(submissionHistory1000Records(now)))
      case "2" => Ok(Json.toJson(submissionHistory70Records(now)))
      case "0" => noRecords422Response
      case _   => Ok(Json.toJson(submissionHistoryFewRecords(now)))
    }

  private def submissionHistoryResponseCommon: SubmissionHistoryResponseCommon =
    SubmissionHistoryResponseCommon(
      regime = "CARF",
      responseParameters = None
    )

  private def submissionHistoryFewRecords(now: Instant): SubmissionHistoryResponse =
    SubmissionHistoryResponse(
      submissionsListResponse = SubmissionsListResponse(
        responseCommon = submissionHistoryResponseCommon,
        responseDetails = SubmissionHistoryResponseDetails(
          submissionsList = SubmissionHistoryTestData.submissionsListFewRecords(now)
        )
      )
    )

  private def submissionHistory70Records(now: Instant): SubmissionHistoryResponse =
    SubmissionHistoryResponse(
      submissionsListResponse = SubmissionsListResponse(
        responseCommon = submissionHistoryResponseCommon,
        responseDetails = SubmissionHistoryResponseDetails(
          submissionsList = SubmissionHistoryTestData.submissionsList70Records(now)
        )
      )
    )

  private def submissionHistory1000Records(now: Instant): SubmissionHistoryResponse =
    SubmissionHistoryResponse(
      submissionsListResponse = SubmissionsListResponse(
        responseCommon = submissionHistoryResponseCommon,
        responseDetails = SubmissionHistoryResponseDetails(
          submissionsList = SubmissionHistoryTestData.submissionsList1000Records(now)
        )
      )
    )

  private def badRequest400Response: Result =
    BadRequest(
      errorDetailJson(
        "400",
        "Invalid JSON document.",
        "instance value (\"FOO\") not found in enum (possible values: [\"BAR\"])"
      )
    )

  private def unprocessableEntity422Response =
    UnprocessableEntity(
      errorDetailJson(
        "422",
        "Unprocessable Entity",
        "999 - Unprocessable Entity"
      )
    )

  private def noRecords422Response =
    UnprocessableEntity(
      errorDetailJson(
        "422",
        "No matching records found for the request",
        "001 - No matching records found for the request"
      )
    )

  private def internalServerError500Response: Result =
    InternalServerError(
      errorDetailJson(
        "500",
        "Internal Server Error",
        "500 - Simulated internal server error from stubs"
      )
    )

  private def serviceUnavailable503Response: Result =
    ServiceUnavailable(
      errorDetailJson(
        "503",
        "Service Unavailable",
        "503 - Simulated service unavailable from stubs"
      )
    )

  private def notAllowedResponse: Result =
    MethodNotAllowed(
      errorDetailJson(
        "405",
        "Method Not Allowed",
        "405 - Simulated method not allowed from stubs"
      )
    )

  private def forbiddenResponse: Result =
    Forbidden(
      errorDetailJson(
        "403",
        "Forbidden",
        "403 - Simulated Forbidden from stubs"
      )
    )
}
