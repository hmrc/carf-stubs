/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.carfstubs.models.response

import play.api.libs.json.{Json, OFormat}

case class IndividualResponse(
    dateOfBirth: Option[String],
    firstName: String,
    lastName: String,
    middleName: Option[String]
)

object IndividualResponse {
  implicit val format: OFormat[IndividualResponse] = Json.format[IndividualResponse]
}
