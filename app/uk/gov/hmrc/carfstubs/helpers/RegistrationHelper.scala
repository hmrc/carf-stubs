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
import play.api.mvc.Results.{InternalServerError, NotFound, Ok}
import uk.gov.hmrc.carfstubs.models.request.{RegisterWithIDRequest, RequestDetail, RegisterOrganisationWithIdResponse}
import uk.gov.hmrc.carfstubs.models.response.*

import java.time.LocalDate

trait RegistrationHelper {

  def returnResponse(request: RegisterWithIDRequest): Result =
    request.requestDetail.IDNumber.take(1) match {
      case "9" => InternalServerError("Unexpected error")
      case "8" => NotFound("Individual user could not be matched")
      case "7" => Ok(Json.toJson(createEmptyIndividualResponse(request)))
      case _   => Ok(Json.toJson(createFullIndividualResponse(request)))
    }

  def returnResponseOrganisation(request: RequestDetail): Result =
    request.IDNumber.take(1) match {
      case "9" => InternalServerError("An unexpected error occurred")
      case "8" => NotFound("The match was unsuccessful")
      //        case "7" => Ok(Json.toJson(createEmptyOrganisationResponse(request)))
      case "1" => Ok(Json.toJson(RegisterOrganisationWithIdResponse))
      case _   => Ok(Json.toJson(createFullOrganisationResponse(request)))
    }

    def createFullOrganisationResponse(request: RequestDetail): RegisterOrganisationWithIdResponse =
      RegisterOrganisationWithIdResponse(
        safeId = "test-safe-id",
        code = Some("0000"),
        organisationName = ("Timmy Ltd"),
        address = AddressResponse(
          addressLine1 = "6 High Street",
          addressLine2 = Some("Birmingham"),
          addressLine3 = Some("Nowhereshire"),
          addressLine4 = Some("Down the road"),
          postalCode = Some("B23 2AZ"),
          countryCode = "GB"
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
              dateOfBirth = Some(request.requestDetail.individual.dateOfBirth),
              firstName = request.requestDetail.individual.firstName,
              lastName = request.requestDetail.individual.lastName,
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
              firstName = request.requestDetail.individual.firstName,
              lastName = request.requestDetail.individual.lastName,
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
}
