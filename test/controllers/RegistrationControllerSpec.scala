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
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.test.Helpers.{contentAsJson, contentAsString, status}
import uk.gov.hmrc.carfstubs.controllers.RegistrationController
import uk.gov.hmrc.carfstubs.models.request.*
import uk.gov.hmrc.carfstubs.models.response.*

import java.time.LocalDate

class RegistrationControllerSpec extends SpecBase {

  val testController: RegistrationController = new RegistrationController(cc)

  val testIndividualNinoRequestModel: RegisterWithIDRequest =
    RegisterWithIDRequest(
      requestCommon = RequestCommon(
        acknowledgementReference = "Test-Ref",
        receiptDate = "Test-ProcessingDate",
        regime = "Test-Regime"
      ),
      requestDetail = RequestDetail(
        requiresNameMatch = false,
        IDNumber = "AB123456C",
        IDType = "NINO",
        individual = Some(
          IndividualDetails(
            firstName = "Professor",
            lastName = "Oak",
            dateOfBirth = Some("Test-DOB")
          )
        ),
        isAnAgent = false,
        organisation = None
      )
    )

  val testIndividualUtrRequestModel: RegisterWithIDRequest =
    RegisterWithIDRequest(
      requestCommon = RequestCommon(
        acknowledgementReference = "Test-Ref",
        receiptDate = "Test-ProcessingDate",
        regime = "Test-Regime"
      ),
      requestDetail = RequestDetail(
        requiresNameMatch = true,
        IDNumber = "1234567890",
        IDType = "UTR",
        individual = Some(
          IndividualDetails(
            firstName = "indiv firstName",
            lastName = "indiv lastName",
            dateOfBirth = Some("2000-01-01")
          )
        ),
        isAnAgent = false,
        organisation = None
      )
    )

  def testIndividualUtrEmptyResponseRequestModel(utrStartNumber: String): RegisterWithIDRequest =
    RegisterWithIDRequest(
      requestCommon = RequestCommon(
        acknowledgementReference = "Test-Ref",
        receiptDate = "Test-ProcessingDate",
        regime = "Test-Regime"
      ),
      requestDetail = RequestDetail(
        requiresNameMatch = true,
        IDNumber = utrStartNumber,
        IDType = "UTR",
        individual = Some(
          IndividualDetails(
            firstName = "indiv Empty firstName",
            lastName = "indiv Empty lastName",
            dateOfBirth = None
          )
        ),
        isAnAgent = false,
        organisation = None
      )
    )

  val testOrganisationRequestModel: RegisterWithIDRequest =
    RegisterWithIDRequest(
      requestCommon = RequestCommon(
        acknowledgementReference = "Test-Ref-Org",
        receiptDate = "Test-ProcessingDate-Org",
        regime = "Test-Regime"
      ),
      requestDetail = RequestDetail(
        requiresNameMatch = true,
        IDNumber = "1234567890",
        IDType = "UTR",
        individual = None,
        isAnAgent = false,
        organisation = Some(OrganisationDetails("The Secret Lab Ltd", "0003"))
      )
    )

  val invalidOrganisationRequestWithNino: RegisterWithIDRequest =
    RegisterWithIDRequest(
      requestCommon = RequestCommon(
        acknowledgementReference = "Test-Ref-Org",
        receiptDate = "Test-ProcessingDate-Org",
        regime = "Test-Regime"
      ),
      requestDetail = RequestDetail(
        requiresNameMatch = true,
        IDNumber = "AB123456C",
        IDType = "NINO",
        individual = None,
        isAnAgent = false,
        organisation = Some(OrganisationDetails("The Secret Lab Ltd", "0003"))
      )
    )

  private def testEmptyResponseInd(firstName: String, lastName: String): JsValue = Json.toJson(
    RegisterWithIDResponse(
      responseCommon = ResponseCommon(
        processingDate = LocalDate.now().toString,
        returnParameters = None,
        status = "OK",
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
              firstName = firstName,
              lastName = lastName,
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

  private def emptyAddress = AddressResponse(
    addressLine1 = "2 Newarre Road",
    addressLine2 = None,
    addressLine3 = None,
    addressLine4 = None,
    postalCode = None,
    countryCode = "GB"
  )

  "RegistrationController" - {
    "register Individual - IDNumber[NINO]" - {
      "must return a 200 OK with a full individual response for a valid NINO (starting with A) " in {
        val originalJson  = Json.toJson(testIndividualNinoRequestModel).as[JsObject]
        val requestDetail = originalJson("requestDetail")
          .as[JsObject] + ("organisation" -> Json.toJson(None: Option[OrganisationDetails]))
        val jsonRequest = originalJson + ("requestDetail" -> requestDetail)
        val result      = testController.register()(fakeRequestWithJsonBody(jsonRequest))
        val resultModel = contentAsJson(result).as[RegisterWithIDResponse]

        status(result)                                          mustBe OK
        resultModel.responseDetail.get.SAFEID                   mustBe "Test-SafeId"
        resultModel.responseDetail.get.individual.get.firstName mustBe "Professor"
        resultModel.responseDetail.get.individual.get.lastName  mustBe "Oak"
        resultModel.responseDetail.get.address.addressLine1     mustBe "2 High Street"
        resultModel.responseDetail.get.address.addressLine2.get mustBe "Birmingham"
      }

      "must return an empty response with the fixed name when the request IDNumber[NINO] starts with a W char" in {
        val result = testController.register()(
          fakeRequestWithJsonBody(
            Json.toJson(
              testIndividualNinoRequestModel.copy(requestDetail =
                testIndividualNinoRequestModel.requestDetail.copy(IDNumber = "WX123456D")
              )
            )
          )
        )
        status(result)        mustBe OK
        contentAsJson(result) mustBe testEmptyResponseInd("Apple", "Pear")
      }

      "must return a not found response when the request IDNumber[NINO] starts with a X char" in {
        val result = testController.register()(
          fakeRequestWithJsonBody(
            Json.toJson(
              testIndividualNinoRequestModel.copy(requestDetail =
                testIndividualNinoRequestModel.requestDetail.copy(IDNumber = "XX123456D")
              )
            )
          )
        )
        status(result)        mustBe NOT_FOUND
        contentAsString(result) must include("The match was unsuccessful")
      }

      "must return an internal server error response when the request IDNumber[NINO] starts with a Y" in {
        val result = testController.register()(
          fakeRequestWithJsonBody(
            Json.toJson(
              testIndividualNinoRequestModel.copy(requestDetail =
                testIndividualNinoRequestModel.requestDetail.copy(IDNumber = "YX123456D")
              )
            )
          )
        )
        status(result)        mustBe INTERNAL_SERVER_ERROR
        contentAsString(result) must include("Unexpected error")
      }
    }

    "register Individual - IDNumber[UTR]" - {
      "must return 200 OK with full individual response when request IDNumber[UTR] starts with '1'" in {
        val request = Json.toJson(testIndividualUtrRequestModel).as[JsObject]
        val result  = testController.register()(fakeRequestWithJsonBody(request))
        status(result) mustBe OK

        val resultModel = contentAsJson(result).as[RegisterWithIDResponse]

        resultModel.responseDetail.get.SAFEID                   mustBe "Test-SafeId"
        resultModel.responseDetail.get.individual.get.firstName mustBe "indiv firstName"
        resultModel.responseDetail.get.individual.get.lastName  mustBe "indiv lastName"
        resultModel.responseDetail.get.address.addressLine1     mustBe "2 High Street"
        resultModel.responseDetail.get.address.addressLine2.get mustBe "Birmingham"
      }

      "must return a 200 OK with an empty response when request IDNumber[UTR] starts with '7' and returns the fixed name" in {
        val request = Json.toJson(testIndividualUtrEmptyResponseRequestModel("7")).as[JsObject]
        val result  = testController.register()(fakeRequestWithJsonBody(request))

        status(result)        mustBe OK
        contentAsString(result) must include("Test-SafeId")
        contentAsJson(result) mustBe testEmptyResponseInd("Apple", "Pear")
      }

      "must return a 200 OK with an non uk response when request IDNumber[UTR] starts with '6' and returns the fixed name" in {
        val request = Json.toJson(testIndividualUtrEmptyResponseRequestModel("6")).as[JsObject]
        val result  = testController.register()(fakeRequestWithJsonBody(request))

        status(result) mustBe OK

        val resultModel = contentAsJson(result).as[RegisterWithIDResponse]

        resultModel.responseDetail.get.address.countryCode mustBe "US"
      }

      "must return a not found response when the request IDNumber[UTR] starts with '8'" in {
        val result = testController.register()(
          fakeRequestWithJsonBody(
            Json.toJson(
              testIndividualUtrRequestModel.copy(requestDetail =
                testIndividualUtrRequestModel.requestDetail.copy(IDNumber = "8234567890")
              )
            )
          )
        )
        status(result)        mustBe NOT_FOUND
        contentAsString(result) must include("The match was unsuccessful")
      }

      "must return an internal server error response when request IDNumber[UTR] starts with '9'" in {
        val result = testController.register()(
          fakeRequestWithJsonBody(
            Json.toJson(
              testIndividualUtrRequestModel.copy(requestDetail =
                testIndividualUtrRequestModel.requestDetail.copy(IDNumber = "9234567890")
              )
            )
          )
        )
        status(result)        mustBe INTERNAL_SERVER_ERROR
        contentAsString(result) must include("Unexpected error")
      }
    }

    "register Organisation" - {
      "must return a 200 OK with a full organisation response for a valid UTR" in {
        val result      = testController.register()(fakeRequestWithJsonBody(Json.toJson(testOrganisationRequestModel)))
        val resultModel = contentAsJson(result).as[RegisterWithIDResponse]

        resultModel.responseDetail.get.SAFEID                                mustBe "Test-SafeId"
        resultModel.responseDetail.get.organisation.get.organisationName     mustBe "The Secret Lab Ltd"
        resultModel.responseDetail.get.organisation.get.organisationType.get mustBe "0003"
        resultModel.responseDetail.get.address.countryCode                   mustBe "GB"
      }

      "must return a 200 OK with a non-UK organisation response when the UTR starts with a 6" in {
        val request     = testOrganisationRequestModel.copy(requestDetail =
          testOrganisationRequestModel.requestDetail.copy(IDNumber = "6123456789")
        )
        val result      = testController.register()(fakeRequestWithJsonBody(Json.toJson(request)))
        val resultModel = contentAsJson(result).as[RegisterWithIDResponse]

        resultModel.responseDetail.get.address.countryCode mustBe "US"
      }

      "must return a 200 OK with an empty organisation response when the UTR starts with a 7" in {
        val request     = testOrganisationRequestModel.copy(requestDetail =
          testOrganisationRequestModel.requestDetail.copy(IDNumber = "7123456789")
        )
        val result      = testController.register()(fakeRequestWithJsonBody(Json.toJson(request)))
        val resultModel = contentAsJson(result).as[RegisterWithIDResponse]

        status(result)                                                   mustBe OK
        resultModel.responseDetail.get.isAnASAgent                       mustBe None
        resultModel.responseDetail.get.organisation.get.organisationType mustBe None
        resultModel.responseDetail.get.organisation.get.code             mustBe Some("0000")
      }

      "must return 404 Not Found for an Organisation when the UTR starts with an 8" in {
        val request = testOrganisationRequestModel.copy(requestDetail =
          testOrganisationRequestModel.requestDetail.copy(IDNumber = "8123456789")
        )
        val result  = testController.register()(fakeRequestWithJsonBody(Json.toJson(request)))

        status(result)          mustBe NOT_FOUND
        contentAsString(result) mustBe "The match was unsuccessful"
      }

      "must return 500 Internal Server Error for an Organisation when the UTR starts with a 9" in {
        val request = testOrganisationRequestModel.copy(requestDetail =
          testOrganisationRequestModel.requestDetail.copy(IDNumber = "9123456789")
        )
        val result  = testController.register()(fakeRequestWithJsonBody(Json.toJson(request)))

        status(result)        mustBe INTERNAL_SERVER_ERROR
        contentAsString(result) must include("Unexpected error")
      }
    }

    "Other errors" - {

      "must return 400 Bad Request when the request JSON is invalid" in {
        val result = testController.register()(fakeRequestWithJsonBody(Json.obj("invalid" -> "payload")))

        status(result)        mustBe BAD_REQUEST
        contentAsString(result) must include("Invalid RegisterWithIDRequest payload")
      }
    }
  }

}
