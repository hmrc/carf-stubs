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

package uk.gov.hmrc.carfstubs.models.response

import play.api.libs.json.{Json, Writes}

case class SubmissionHistoryResponse(submissionsListResponse: SubmissionsListResponse)

object SubmissionHistoryResponse {
  implicit val writes: Writes[SubmissionHistoryResponse] = Json.writes[SubmissionHistoryResponse]
}

case class SubmissionsListResponse(
    responseCommon: SubmissionHistoryResponseCommon,
    responseDetails: SubmissionHistoryResponseDetails
)

object SubmissionsListResponse {
  implicit val writes: Writes[SubmissionsListResponse] = Json.writes[SubmissionsListResponse]
}

case class SubmissionHistoryResponseCommon(
    regime: String,
    responseParameters: Option[List[SubmissionHistoryResponseParameter]]
)

object SubmissionHistoryResponseCommon {
  implicit val writes: Writes[SubmissionHistoryResponseCommon] = Json.writes[SubmissionHistoryResponseCommon]
}

case class SubmissionHistoryResponseParameter(paramName: String, paramValue: String)

object SubmissionHistoryResponseParameter {
  implicit val writes: Writes[SubmissionHistoryResponseParameter] = Json.writes[SubmissionHistoryResponseParameter]
}

case class SubmissionHistoryResponseDetails(submissionsList: Seq[SubmissionHistoryRecord])

object SubmissionHistoryResponseDetails {
  implicit val writes: Writes[SubmissionHistoryResponseDetails] = Json.writes[SubmissionHistoryResponseDetails]
}
