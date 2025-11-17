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

package uk.gov.hmrc.carfstubs.helpers

import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.mvc.Results.{BadRequest, InternalServerError, NotFound, Ok}
import uk.gov.hmrc.carfstubs.models.request.{RegisterWithIDRequest, RequestDetail}
import uk.gov.hmrc.carfstubs.models.response.*

import java.time.LocalDate

trait RegistrationHelper {

  def returnResponse(request: RegisterWithIDRequest): Result = {

    val idNumber = request.requestDetail.IDNumber
    val idType   = request.requestDetail.IDType

    (idType, idNumber.take(1)) match {
      case (_, "9") => InternalServerError("Unexpected error")
      case (_, "8") => NotFound("The match was unsuccessful")

      case ("UTR", "7") => Ok(Json.toJson(createEmptyOrganisationResponse(request)))
      case ("UTR", "6") => Ok(Json.toJson(createNonUkOrganisationResponse(request)))
      case ("UTR", _)   => Ok(Json.toJson(createFullOrganisationResponse(request)))

      case ("NINO", "7") => Ok(Json.toJson(createEmptyIndividualResponse(request)))
      case ("NINO", _)   => Ok(Json.toJson(createFullIndividualResponse(request)))

      case _ => BadRequest(s"Invalid IDType: $idType")

    }
  }

  private def createFullOrganisationResponse(request: RegisterWithIDRequest): RegisterWithIDResponse =
    RegisterWithIDResponse(
      responseCommon = ResponseCommon(
        processingDate = LocalDate.now().toString,
        returnParameters = None,
        status = "OK",
        statusText = None
      ),
      responseDetail = Some(
        ResponseDetail(
          ARN = "",
          SAFEID = "Test-SafeId",
          address = fullAddress,
          contactDetails = ContactDetails(None, None, None, None),
          individual = None,
          isAnASAgent = Some(false),
          isAnAgent = false,
          isAnIndividual = false,
          isEditable = false,
          organisation = Some(
            OrganisationResponse(
              organisationName =
                request.requestDetail.organisation.map(_.organisationName).getOrElse("AutoMatched Org Ltd"),
              code = request.requestDetail.organisation.map(_.organisationType),
              isAGroup = false,
              organisationType = request.requestDetail.organisation.map(_.organisationType)
            )
          )
        )
      )
    )

  private def createEmptyOrganisationResponse(request: RegisterWithIDRequest): RegisterWithIDResponse =
    RegisterWithIDResponse(
      responseCommon = ResponseCommon(
        processingDate = LocalDate.now().toString,
        returnParameters = None,
        status = "OK",
        statusText = None
      ),
      responseDetail = Some(
        ResponseDetail(
          ARN = "",
          SAFEID = "Test-SafeId",
          address = emptyAddress,
          contactDetails = ContactDetails(None, None, None, None),
          individual = None,
          isAnASAgent = Some(false),
          isAnAgent = false,
          isAnIndividual = false,
          isEditable = false,
          organisation = Some(
            OrganisationResponse(
              organisationName = request.requestDetail.organisation.map(_.organisationName).getOrElse("Empty Org Ltd"),
              code = Some("0000"),
              isAGroup = false,
              organisationType = request.requestDetail.organisation.map(_.organisationType)
            )
          )
        )
      )
    )

  private def createNonUkOrganisationResponse(request: RegisterWithIDRequest): RegisterWithIDResponse =
    RegisterWithIDResponse(
      responseCommon = ResponseCommon(
        processingDate = LocalDate.now().toString,
        returnParameters = None,
        status = "OK",
        statusText = None
      ),
      responseDetail = Some(
        ResponseDetail(
          ARN = "",
          SAFEID = "Test-SafeId",
          address = nonUkAddress,
          contactDetails = ContactDetails(None, None, None, None),
          individual = None,
          isAnASAgent = Some(false),
          isAnAgent = false,
          isAnIndividual = false,
          isEditable = false,
          organisation = Some(
            OrganisationResponse(
              organisationName = request.requestDetail.organisation.map(_.organisationName).getOrElse("Outside Org"),
              code = Some("0000"),
              isAGroup = false,
              organisationType = request.requestDetail.organisation.map(_.organisationType)
            )
          )
        )
      )
    )

  private def createFullIndividualResponse(request: RegisterWithIDRequest): RegisterWithIDResponse =
    RegisterWithIDResponse(
      responseCommon = ResponseCommon(
        processingDate = LocalDate.now().toString,
        returnParameters = Some(List(ReturnParameters(paramName = "Test-ParamName", paramValue = "Test-ParamValue"))),
        status = "200",
        statusText = Some("Test-StatusText")
      ),
      responseDetail = Some(
        ResponseDetail(
          ARN = "Test-ARN",
          SAFEID = "Test-SafeId",
          address = fullAddress,
          contactDetails = ContactDetails(
            emailAddress = Some("test@example.com"),
            faxNumber = Some("Test-FaxNo"),
            mobileNumber = Some("Test-MobileNo"),
            phoneNumber = Some("TestPhoneNo")
          ),
          individual = Some(
            IndividualResponse(
              dateOfBirth = request.requestDetail.individual.map(_.dateOfBirth),
              firstName = request.requestDetail.individual.map(_.firstName).getOrElse("Ind First Name"),
              lastName = request.requestDetail.individual.map(_.lastName).getOrElse("Ind Last Name"),
              middleName = Some("Bjorn")
            )
          ),
          isAnASAgent = Some(false),
          isAnAgent = request.requestDetail.isAnAgent,
          isAnIndividual = true,
          isEditable = false,
          organisation = None
        )
      )
    )

  private def createEmptyIndividualResponse(request: RegisterWithIDRequest): RegisterWithIDResponse =
    RegisterWithIDResponse(
      responseCommon = ResponseCommon(
        processingDate = LocalDate.now().toString,
        returnParameters = None,
        status = "200",
        statusText = None
      ),
      responseDetail = Some(
        ResponseDetail(
          ARN = "Test-ARN",
          SAFEID = "Test-SafeId",
          address = emptyAddress,
          contactDetails = ContactDetails(
            emailAddress = None,
            faxNumber = None,
            mobileNumber = None,
            phoneNumber = None
          ),
          individual = Some(
            IndividualResponse(
              dateOfBirth = None,
              firstName = request.requestDetail.individual.map(_.firstName).getOrElse("Ind First Name"),
              lastName = request.requestDetail.individual.map(_.lastName).getOrElse("Ind Last Name"),
              middleName = None
            )
          ),
          isAnASAgent = None,
          isAnAgent = request.requestDetail.isAnAgent,
          isAnIndividual = true,
          isEditable = false,
          organisation = None
        )
      )
    )

  private def fullAddress = AddressResponse(
    addressLine1 = "2 High Street",
    addressLine2 = Some("Birmingham"),
    addressLine3 = Some("Nowhereshire"),
    addressLine4 = Some("Down the road"),
    postalCode = Some("B23 2AZ"),
    countryCode = "GB"
  )

  private def emptyAddress = AddressResponse(
    addressLine1 = "2 Newarre Road",
    addressLine2 = None,
    addressLine3 = None,
    addressLine4 = None,
    postalCode = None,
    countryCode = "GB"
  )

  private def nonUkAddress = AddressResponse(
    addressLine1 = "123 Big Apple",
    addressLine2 = Some("New York"),
    addressLine3 = None,
    addressLine4 = None,
    postalCode = Some("BNY 2AZ"),
    countryCode = "US"
  )
}
