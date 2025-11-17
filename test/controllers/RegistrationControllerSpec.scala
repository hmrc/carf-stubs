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

package controllers

import base.SpecBase
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers.{contentAsJson, contentAsString, status}
import uk.gov.hmrc.carfstubs.controllers.RegistrationController
import uk.gov.hmrc.carfstubs.models.request.{IndividualDetails, RegisterWithIDRequest, RequestCommon, RequestDetail}
import uk.gov.hmrc.carfstubs.models.response.*

import java.time.LocalDate

class RegistrationControllerSpec extends SpecBase {

  val testController: RegistrationController = new RegistrationController(cc)

  private val testFullResponse: JsValue = Json.toJson(
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
              dateOfBirth = Some("Test-DOB"),
              firstName = "Professor",
              lastName = "Oak",
              middleName = Some("Bjorn")
            )
          ),
          isAnASAgent = Some(false),
          isAnAgent = false,
          isAnIndividual = true,
          isEditable = false,
          organisation = None
        )
      )
    )
  )

  private val testEmptyResponse: JsValue = Json.toJson(
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
              firstName = "Professor",
              lastName = "Oak",
              middleName = None
            )
          ),
          isAnASAgent = None,
          isAnAgent = false,
          isAnIndividual = true,
          isEditable = false,
          organisation = None
        )
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

  val testRequestModel =
    RegisterWithIDRequest(
      requestCommon = RequestCommon(
        acknowledgementReference = "Test-Ref",
        receiptDate = "Test-ProcessingDate",
        regime = "Test-Regime"
      ),
      requestDetail = RequestDetail(
        requiresNameMatch = false,
        IDNumber = "Test-IDNumber",
        IDType = "Test-IDType",
        individual = IndividualDetails("Test-DOB", "Professor", "Oak"),
        isAnAgent = false
      )
    )

  "RegistrationController" - {
    "registerIndividualWithId" - {
      "must return a full response when the request IDNumber starts with a non 7,8 or 9 char" in {
        val result = testController.registerIndividualWithId()(fakeRequestWithJsonBody(Json.toJson(testRequestModel)))

        status(result)        mustBe OK
        contentAsJson(result) mustBe testFullResponse
      }

      "must return a full response when the request IDNumber[NINO] starts with a J char" in {
        val result = testController.registerIndividualWithId()(
          fakeRequestWithJsonBody(
            Json.toJson(
              testRequestModel.copy(requestDetail = testRequestModel.requestDetail.copy(IDNumber = "JX123456D"))
            )
          )
        )

        status(result)        mustBe OK
        contentAsJson(result) mustBe testFullResponse
      }

      "must return an empty response when the request IDNumber starts with a 7" in {
        val result = testController.registerIndividualWithId()(
          fakeRequestWithJsonBody(
            Json.toJson(testRequestModel.copy(requestDetail = testRequestModel.requestDetail.copy(IDNumber = "7123")))
          )
        )

        status(result)        mustBe OK
        contentAsJson(result) mustBe testEmptyResponse
      }

      "must return an empty response when the request IDNumber[NINO] starts with a W char" in {
        val result = testController.registerIndividualWithId()(
          fakeRequestWithJsonBody(
            Json.toJson(
              testRequestModel.copy(requestDetail = testRequestModel.requestDetail.copy(IDNumber = "WX123456D"))
            )
          )
        )

        status(result)        mustBe OK
        contentAsJson(result) mustBe testEmptyResponse
      }

      "must return a not found response when the request IDNumber[NINO] starts with a X char" in {
        val result = testController.registerIndividualWithId()(
          fakeRequestWithJsonBody(
            Json.toJson(
              testRequestModel.copy(requestDetail = testRequestModel.requestDetail.copy(IDNumber = "XX123456D"))
            )
          )
        )

        status(result)        mustBe NOT_FOUND
        contentAsString(result) must include("Individual user could not be matched")
      }

      "must return a not found response when the request IDNumber starts with an 8" in {
        val result = testController.registerIndividualWithId()(
          fakeRequestWithJsonBody(
            Json.toJson(testRequestModel.copy(requestDetail = testRequestModel.requestDetail.copy(IDNumber = "8123")))
          )
        )

        status(result)        mustBe NOT_FOUND
        contentAsString(result) must include("Individual user could not be matched")
      }

      "must return an internal server error response when the request IDNumber starts with an 9" in {
        val result = testController.registerIndividualWithId()(
          fakeRequestWithJsonBody(
            Json.toJson(testRequestModel.copy(requestDetail = testRequestModel.requestDetail.copy(IDNumber = "9123")))
          )
        )

        status(result)        mustBe INTERNAL_SERVER_ERROR
        contentAsString(result) must include("Unexpected error")
      }

      "must return an internal server error response when the request IDNumber[NINO] starts with an Y char" in {
        val result = testController.registerIndividualWithId()(
          fakeRequestWithJsonBody(
            Json.toJson(
              testRequestModel.copy(requestDetail = testRequestModel.requestDetail.copy(IDNumber = "YX123456D"))
            )
          )
        )

        status(result)        mustBe INTERNAL_SERVER_ERROR
        contentAsString(result) must include("Unexpected error")
      }

      "must return bad request when the request is not valid" in {
        val result = testController.registerIndividualWithId()(fakeRequestWithJsonBody(Json.toJson("invalid timmy")))

        status(result)               mustBe BAD_REQUEST
        contentAsJson(result).toString must include("Invalid RegisterWithIDRequest payload")
      }
    }
  }
}
