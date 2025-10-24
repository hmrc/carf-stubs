/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.carfstubs.models.request

import play.api.libs.json.{Json, OFormat}

case class IndividualDetails(dateOfBirth: String, firstName: String, lastName: String)

object IndividualDetails {
  implicit val format: OFormat[IndividualDetails] = Json.format[IndividualDetails]
}
