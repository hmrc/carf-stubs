/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.carfstubs.models.response

import play.api.libs.json.{Json, OFormat}

case class ResponseDetail(
    ARN: String,
    SAFEID: String,
    address: AddressResponse,
    contactDetails: ContactDetails,
    individual: Option[IndividualResponse],
    isAnASAgent: Option[Boolean],
    isAnAgent: Boolean,
    isAnIndividual: Boolean,
    isEditable: Boolean,
    organisation: Option[OrganisationResponse]
)

object ResponseDetail {
  implicit val format: OFormat[ResponseDetail] = Json.format[ResponseDetail]
}

case class ContactDetails(
    emailAddress: Option[String],
    faxNumber: Option[String],
    mobileNumber: Option[String],
    phoneNumber: Option[String]
)

object ContactDetails {
  implicit val format: OFormat[ContactDetails] = Json.format[ContactDetails]
}
