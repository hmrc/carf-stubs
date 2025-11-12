/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.carfstubs.models.request

import play.api.libs.json.*

case class RequestDetail(
    requiresNameMatch: Boolean,
    IDNumber: String,
    IDType: String,
    individual: IndividualDetails,
    isAnAgent: Boolean,
    organisation: Option[OrganisationDetails]
)

object RequestDetail {
  implicit val format: OFormat[RequestDetail] = Json.format[RequestDetail]
}

case class OrganisationDetails(organisationName: String, organisationType: String)

object OrganisationDetails {
  implicit val format: OFormat[OrganisationDetails] = Json.format[OrganisationDetails]
}

sealed trait OrganisationType {
  def code: String
}

object OrganisationType {
  case object LimitedCompany extends OrganisationType {
    val code = "0000"
  }

  case object SoleTrader extends OrganisationType {
    val code = "0001"
  }

  case object Partnership extends OrganisationType {
    val code = "0002"
  }

  case object LimitedLiabilityPartnership extends OrganisationType {
    val code = "0003"
  }

  case object UnincorporatedBody extends OrganisationType {
    val code = "0004"
  }

  val values: Seq[OrganisationType] = Seq(
    LimitedCompany,
    SoleTrader,
    Partnership,
    LimitedLiabilityPartnership,
    UnincorporatedBody
  )

  def fromCode(code: String): Option[OrganisationType] =
    values.find(_.code == code)

  implicit val format: Format[OrganisationType] = new Format[OrganisationType] {
    def reads(json: JsValue): JsResult[OrganisationType] =
      json.validate[String].flatMap { code =>
        fromCode(code).map(JsSuccess(_)).getOrElse(JsError(s"Invalid organisation type code: $code"))
      }

    def writes(orgType: OrganisationType): JsValue = JsString(orgType.code)
  }
}
