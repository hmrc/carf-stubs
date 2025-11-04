/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.carfstubs.models.response

import play.api.libs.json.{Json, OFormat}

case class ResponseCommon(
    processingDate: String,
    returnParameters: Option[List[ReturnParameters]],
    status: String,
    statusText: Option[String]
)

object ResponseCommon {
  implicit val format: OFormat[ResponseCommon] = Json.format[ResponseCommon]
}

case class ReturnParameters(paramName: String, paramValue: String)

object ReturnParameters {
  implicit val format: OFormat[ReturnParameters] = Json.format[ReturnParameters]
}
