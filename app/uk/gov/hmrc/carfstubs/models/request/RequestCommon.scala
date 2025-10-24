/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.carfstubs.models.request

import play.api.libs.json.{Json, OFormat}

case class RequestCommon(acknowledgementReference: String, receiptDate: String, regime: String)

object RequestCommon {
  implicit val format: OFormat[RequestCommon] = Json.format[RequestCommon]
}
