/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.carfstubs.models.response

import play.api.libs.json.{Json, OFormat}

case class RegisterWithIDResponse(responseCommon: ResponseCommon, responseDetail: Option[ResponseDetail])

object RegisterWithIDResponse {
  implicit val format: OFormat[RegisterWithIDResponse] = Json.format[RegisterWithIDResponse]
}
