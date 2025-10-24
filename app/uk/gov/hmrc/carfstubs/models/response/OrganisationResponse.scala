/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.carfstubs.models.response

import play.api.libs.json.{Json, OFormat}

case class OrganisationResponse(
    code: Option[String],
    isAGroup: Boolean,
    organisationName: String,
    organisationType: Option[String]
)

object OrganisationResponse {
  implicit val format: OFormat[OrganisationResponse] = Json.format[OrganisationResponse]
}
