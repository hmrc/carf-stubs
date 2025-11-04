/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.carfstubs.models.request

import play.api.libs.json.{Json, OFormat}

case class RegisterWithIDRequest(requestCommon: RequestCommon, requestDetail: RequestDetail)

object RegisterWithIDRequest {
  implicit val format: OFormat[RegisterWithIDRequest] = Json.format[RegisterWithIDRequest]
}
