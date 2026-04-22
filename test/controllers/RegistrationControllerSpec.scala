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
import play.api.http.Status.*
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.test.Helpers.{contentAsJson, contentAsString, status}
import uk.gov.hmrc.carfstubs.controllers.RegistrationController
import uk.gov.hmrc.carfstubs.models.request.*
import uk.gov.hmrc.carfstubs.models.response.*

class RegistrationControllerSpec extends SpecBase {

  val testController: RegistrationController = new RegistrationController(cc)

  val testIndividualNinoRequestModel: RegisterWithIDApiRequest = RegisterWithIDApiRequest(
    registerWithIDRequest = RegisterWithIDRequest(
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
  )

  val testIndividualUtrRequestModel: RegisterWithIDApiRequest = RegisterWithIDApiRequest(
    registerWithIDRequest = RegisterWithIDRequest(
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
  )

  def testIndividualUtrEmptyResponseRequestModel(utrStartNumber: String): RegisterWithIDApiRequest =
    RegisterWithIDApiRequest(
      registerWithIDRequest = RegisterWithIDRequest(
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
    )

  val testOrganisationRequestModel: RegisterWithIDApiRequest = RegisterWithIDApiRequest(
    registerWithIDRequest = RegisterWithIDRequest(
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
  )

  val invalidOrganisationRequestWithNino: RegisterWithIDApiRequest = RegisterWithIDApiRequest(
    registerWithIDRequest = RegisterWithIDRequest(
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
  )

  private val nonUkCountryCodes = List("US", "FR", "DE", "CH", "JE")
  private val testRepeater      = List(false, false, false)

  private def validIndividualWithoutIdRequestJson(firstName: String = "John"): JsValue = Json.parse(
    s"""{
      "registerWithoutIDRequest": {
        "requestCommon": {
          "acknowledgementReference": "Test-Ref-WithoutId",
          "receiptDate": "2024-01-01T00:00:00Z",
          "regime": "CARF"
        },
        "requestDetail": {
          "individual": {
            "firstName": ${Json.toJson(firstName)},
            "lastName": "Doe",
            "dateOfBirth": "1990-01-01"
          },
          "address": {
            "addressLine1": "10 Test Street",
            "addressLine2": "Testington",
            "postalCode": "AB1 2CD",
            "countryCode": "GB"
          },
          "contactDetails": {
            "emailAddress": "john.doe@example.com",
            "phoneNumber": "01234567890"
          },
          "IsAnAgent": false,
          "IsAGroup": false
        }
      }
    }"""
  )

  private def validOrganisationWithoutIdRequestJson(organisationName: String = "Apples LTD"): JsValue = Json.parse(
    s"""{
        "registerWithoutIDRequest": {
          "requestCommon": {
            "acknowledgementReference": "Test-Ref-WithoutId",
            "receiptDate": "2024-01-01T00:00:00Z",
            "regime": "CARF"
          },
          "requestDetail": {
            "organisation": {
              "organisationName": ${Json.toJson(organisationName)}
            },
            "address": {
              "addressLine1": "10 Test Street",
              "addressLine2": "Testington",
              "postalCode": "AB1 2CD",
              "countryCode": "GB"
            },
            "contactDetails": {
              "emailAddress": "john.doe@example.com",
              "phoneNumber": "01234567890"
            },
            "IsAnAgent": false,
            "IsAGroup": false
          }
        }
      }"""
  )

  "RegistrationController" - {
    "register Individual - IDNumber[NINO]" - {
      "must return a 200 OK with a full individual response for a valid NINO (starting with A) " in {
        val result      = testController.register()(fakeRequestWithJsonBody(Json.toJson(testIndividualNinoRequestModel)))
        val resultModel = contentAsJson(result).as[RegisterWithIdResponse]

        status(result)                                                                 mustBe OK
        resultModel.registerWithIDResponse.responseDetail.get.SAFEID                     must not be empty
        resultModel.registerWithIDResponse.responseDetail.get.individual.get.firstName mustBe "Professor"
        resultModel.registerWithIDResponse.responseDetail.get.individual.get.lastName  mustBe "Oak"
        resultModel.registerWithIDResponse.responseDetail.get.address.addressLine1     mustBe "2 High Street"
        resultModel.registerWithIDResponse.responseDetail.get.address.addressLine2.get mustBe "Birmingham"
      }

      "must return an empty response with the fixed name when the request IDNumber[NINO] starts with a W char" in {
        val result      = testController.register()(
          fakeRequestWithJsonBody(
            Json.toJson(
              testIndividualNinoRequestModel.copy(registerWithIDRequest =
                testIndividualNinoRequestModel.registerWithIDRequest.copy(requestDetail =
                  testIndividualNinoRequestModel.registerWithIDRequest.requestDetail.copy(IDNumber = "WX123456D")
                )
              )
            )
          )
        )
        val resultModel = contentAsJson(result).as[RegisterWithIdResponse]

        status(result) mustBe OK

        resultModel.registerWithIDResponse.responseDetail.get.SAFEID                        must not be empty
        resultModel.registerWithIDResponse.responseDetail.get.individual.get.firstName    mustBe "Apple"
        resultModel.registerWithIDResponse.responseDetail.get.individual.get.lastName     mustBe "Pear"
        resultModel.registerWithIDResponse.responseDetail.get.contactDetails.emailAddress mustBe None
      }

      "must return a not found response when the request IDNumber[NINO] starts with a X char" in {
        val result = testController.register()(
          fakeRequestWithJsonBody(
            Json.toJson(
              testIndividualNinoRequestModel.copy(registerWithIDRequest =
                testIndividualNinoRequestModel.registerWithIDRequest.copy(requestDetail =
                  testIndividualNinoRequestModel.registerWithIDRequest.requestDetail.copy(IDNumber = "XX123456D")
                )
              )
            )
          )
        )
        status(result)        mustBe NOT_FOUND
        contentAsString(result) must include("The match was unsuccessful")
      }

      "must return an unprocessable entity error response when the request IDNumber[NINO] starts with a T" in {
        val result = testController.register()(
          fakeRequestWithJsonBody(
            Json.toJson(
              testIndividualNinoRequestModel.copy(registerWithIDRequest =
                testIndividualNinoRequestModel.registerWithIDRequest.copy(requestDetail =
                  testIndividualNinoRequestModel.registerWithIDRequest.requestDetail.copy(IDNumber = "TX123456D")
                )
              )
            )
          )
        )
        status(result)        mustBe UNPROCESSABLE_ENTITY
        contentAsString(result) must include("Unprocessable entity")
      }

      "must return an internal server error response when the request IDNumber[NINO] starts with a Y" in {
        val result = testController.register()(
          fakeRequestWithJsonBody(
            Json.toJson(
              testIndividualNinoRequestModel.copy(registerWithIDRequest =
                testIndividualNinoRequestModel.registerWithIDRequest.copy(requestDetail =
                  testIndividualNinoRequestModel.registerWithIDRequest.requestDetail.copy(IDNumber = "YX123456D")
                )
              )
            )
          )
        )
        status(result)        mustBe INTERNAL_SERVER_ERROR
        contentAsString(result) must include("Unexpected error")
      }

      "must return a service unavailable error response when the request IDNumber[NINO] starts with a S" in {
        val result = testController.register()(
          fakeRequestWithJsonBody(
            Json.toJson(
              testIndividualNinoRequestModel.copy(registerWithIDRequest =
                testIndividualNinoRequestModel.registerWithIDRequest.copy(requestDetail =
                  testIndividualNinoRequestModel.registerWithIDRequest.requestDetail.copy(IDNumber = "SX123456D")
                )
              )
            )
          )
        )
        status(result)        mustBe SERVICE_UNAVAILABLE
        contentAsString(result) must include("Service unavailable")
      }

    }

    "register Individual - IDNumber[UTR]" - {
      "must return 200 OK with full individual response when request IDNumber[UTR] starts with '1'" in {
        val request = Json.toJson(testIndividualUtrRequestModel).as[JsObject]
        val result  = testController.register()(fakeRequestWithJsonBody(request))
        status(result) mustBe OK
        val resultModel = contentAsJson(result).as[RegisterWithIdResponse]

        resultModel.registerWithIDResponse.responseDetail.get.SAFEID                     must not be empty
        resultModel.registerWithIDResponse.responseDetail.get.individual.get.firstName mustBe "indiv firstName"
        resultModel.registerWithIDResponse.responseDetail.get.individual.get.lastName  mustBe "indiv lastName"
        resultModel.registerWithIDResponse.responseDetail.get.address.addressLine1     mustBe "2 High Street"
        resultModel.registerWithIDResponse.responseDetail.get.address.addressLine2.get mustBe "Birmingham"
      }

      "must return a 200 OK with an empty response when request IDNumber[UTR] starts with '7' and returns the fixed name" in {
        val request = Json.toJson(testIndividualUtrEmptyResponseRequestModel("7")).as[JsObject]
        val result  = testController.register()(fakeRequestWithJsonBody(request))

        status(result) mustBe OK

        val resultModel = contentAsJson(result).as[RegisterWithIdResponse]

        resultModel.registerWithIDResponse.responseDetail.get.SAFEID                     must not be empty
        resultModel.registerWithIDResponse.responseDetail.get.individual.get.firstName mustBe "Apple"
        resultModel.registerWithIDResponse.responseDetail.get.individual.get.lastName  mustBe "Pear"
      }

      "must return a 200 OK with an non uk response when request IDNumber[UTR] starts with '6' and returns the fixed name" in {
        val outcome = testRepeater.foldLeft(true) { (previousResult, _) =>
          val request = Json.toJson(testIndividualUtrEmptyResponseRequestModel("6")).as[JsObject]
          val result  = testController.register()(fakeRequestWithJsonBody(request))

          status(result) mustBe OK
          val resultModel = contentAsJson(result).as[RegisterWithIdResponse]

          previousResult && nonUkCountryCodes.contains(
            resultModel.registerWithIDResponse.responseDetail.get.address.countryCode
          )
        }

        outcome mustBe true
      }

      "must return a not found response when the request IDNumber[UTR] starts with '8'" in {
        val result = testController.register()(
          fakeRequestWithJsonBody(
            Json.toJson(
              testIndividualUtrRequestModel.copy(registerWithIDRequest =
                testIndividualNinoRequestModel.registerWithIDRequest.copy(requestDetail =
                  testIndividualUtrRequestModel.registerWithIDRequest.requestDetail.copy(IDNumber = "8234567890")
                )
              )
            )
          )
        )
        status(result)        mustBe NOT_FOUND
        contentAsString(result) must include("The match was unsuccessful")
      }

      "must return an unprocessable entity error response when the request IDNumber[UTR] starts with a 5" in {
        val result = testController.register()(
          fakeRequestWithJsonBody(
            Json.toJson(
              testIndividualUtrRequestModel.copy(registerWithIDRequest =
                testIndividualNinoRequestModel.registerWithIDRequest.copy(requestDetail =
                  testIndividualUtrRequestModel.registerWithIDRequest.requestDetail.copy(IDNumber = "5234567890")
                )
              )
            )
          )
        )
        status(result)        mustBe UNPROCESSABLE_ENTITY
        contentAsString(result) must include("Unprocessable entity")
      }

      "must return an internal server error response when request IDNumber[UTR] starts with '9'" in {
        val result = testController.register()(
          fakeRequestWithJsonBody(
            Json.toJson(
              testIndividualUtrRequestModel.copy(registerWithIDRequest =
                testIndividualNinoRequestModel.registerWithIDRequest.copy(requestDetail =
                  testIndividualUtrRequestModel.registerWithIDRequest.requestDetail.copy(IDNumber = "9234567890")
                )
              )
            )
          )
        )
        status(result)        mustBe INTERNAL_SERVER_ERROR
        contentAsString(result) must include("Unexpected error")
      }

      "must return a service unavailable error response when the request IDNumber[UTR] starts with a 4" in {
        val result = testController.register()(
          fakeRequestWithJsonBody(
            Json.toJson(
              testIndividualNinoRequestModel.copy(registerWithIDRequest =
                testIndividualNinoRequestModel.registerWithIDRequest.copy(requestDetail =
                  testIndividualNinoRequestModel.registerWithIDRequest.requestDetail.copy(IDNumber = "4234567890")
                )
              )
            )
          )
        )
        status(result)        mustBe SERVICE_UNAVAILABLE
        contentAsString(result) must include("Service unavailable")
      }

    }

    "register Organisation" - {
      "must return a 200 OK with a full organisation response for a valid UTR" in {
        val result      = testController.register()(fakeRequestWithJsonBody(Json.toJson(testOrganisationRequestModel)))
        val resultModel = contentAsJson(result).as[RegisterWithIdResponse]

        resultModel.registerWithIDResponse.responseDetail.get.SAFEID                                  must not be empty
        resultModel.registerWithIDResponse.responseDetail.get.organisation.get.organisationName     mustBe "The Secret Lab Ltd"
        resultModel.registerWithIDResponse.responseDetail.get.organisation.get.organisationType.get mustBe "0003"
        resultModel.registerWithIDResponse.responseDetail.get.address.countryCode                   mustBe "GB"
      }

      "must return a 200 OK with a non-UK organisation response when the UTR starts with a 6" in {

        val outcome = testRepeater.foldLeft(true) { (previousResult, _) =>
          val request     = testOrganisationRequestModel.copy(registerWithIDRequest =
            testIndividualNinoRequestModel.registerWithIDRequest.copy(requestDetail =
              testOrganisationRequestModel.registerWithIDRequest.requestDetail.copy(IDNumber = "6123456789")
            )
          )
          val result      = testController.register()(fakeRequestWithJsonBody(Json.toJson(request)))
          val resultModel = contentAsJson(result).as[RegisterWithIdResponse]

          previousResult && nonUkCountryCodes.contains(
            resultModel.registerWithIDResponse.responseDetail.get.address.countryCode
          )

        }
        outcome mustBe true
      }

      "must return a 200 OK with a invalid country code organisation response when the UTR starts with a 68" in {
        val request     = testOrganisationRequestModel.copy(registerWithIDRequest =
          testIndividualNinoRequestModel.registerWithIDRequest.copy(requestDetail =
            testOrganisationRequestModel.registerWithIDRequest.requestDetail.copy(IDNumber = "6823456789")
          )
        )
        val result      = testController.register()(fakeRequestWithJsonBody(Json.toJson(request)))
        val resultModel = contentAsJson(result).as[RegisterWithIdResponse]

        resultModel.registerWithIDResponse.responseDetail.get.address.countryCode mustEqual "ZX"
      }

      "must return a 200 OK with an empty organisation response when the UTR starts with a 7" in {
        val request     = testOrganisationRequestModel.copy(registerWithIDRequest =
          testIndividualNinoRequestModel.registerWithIDRequest.copy(requestDetail =
            testOrganisationRequestModel.registerWithIDRequest.requestDetail.copy(IDNumber = "7123456789")
          )
        )
        val result      = testController.register()(fakeRequestWithJsonBody(Json.toJson(request)))
        val resultModel = contentAsJson(result).as[RegisterWithIdResponse]

        status(result)                                                                          mustBe OK
        resultModel.registerWithIDResponse.responseDetail.get.isAnASAgent                       mustBe None
        resultModel.registerWithIDResponse.responseDetail.get.organisation.get.organisationType mustBe None
        resultModel.registerWithIDResponse.responseDetail.get.organisation.get.code             mustBe Some("0000")
      }

      "must return 404 Not Found for an Organisation when the UTR starts with an 8" in {
        val request = testOrganisationRequestModel.copy(registerWithIDRequest =
          testIndividualNinoRequestModel.registerWithIDRequest.copy(requestDetail =
            testOrganisationRequestModel.registerWithIDRequest.requestDetail.copy(IDNumber = "8123456789")
          )
        )
        val result  = testController.register()(fakeRequestWithJsonBody(Json.toJson(request)))

        status(result)          mustBe NOT_FOUND
        contentAsString(result) mustBe "The match was unsuccessful"
      }

      "must return 422 Unprocessable Entity Error for an Organisation when the UTR starts with a 5" in {
        val request = testOrganisationRequestModel.copy(registerWithIDRequest =
          testIndividualNinoRequestModel.registerWithIDRequest.copy(requestDetail =
            testOrganisationRequestModel.registerWithIDRequest.requestDetail.copy(IDNumber = "5123456789")
          )
        )
        val result  = testController.register()(fakeRequestWithJsonBody(Json.toJson(request)))

        status(result)        mustBe UNPROCESSABLE_ENTITY
        contentAsString(result) must include("Unprocessable entity")
      }

      "must return 500 Internal Server Error for an Organisation when the UTR starts with a 9" in {
        val request = testOrganisationRequestModel.copy(registerWithIDRequest =
          testIndividualNinoRequestModel.registerWithIDRequest.copy(requestDetail =
            testOrganisationRequestModel.registerWithIDRequest.requestDetail.copy(IDNumber = "9123456789")
          )
        )
        val result  = testController.register()(fakeRequestWithJsonBody(Json.toJson(request)))

        status(result)        mustBe INTERNAL_SERVER_ERROR
        contentAsString(result) must include("Unexpected error")
      }

      "must return 503 Service Unavailable Error for an Organisation when the UTR starts with a 4" in {
        val request = testOrganisationRequestModel.copy(registerWithIDRequest =
          testIndividualNinoRequestModel.registerWithIDRequest.copy(requestDetail =
            testOrganisationRequestModel.registerWithIDRequest.requestDetail.copy(IDNumber = "4123456789")
          )
        )
        val result  = testController.register()(fakeRequestWithJsonBody(Json.toJson(request)))

        status(result)        mustBe SERVICE_UNAVAILABLE
        contentAsString(result) must include("Service unavailable")
      }

    }

    "register Individual Without ID" - {

      "must return 200 OK with SAFEID for a valid request" in {
        val result = testController.registerWithoutId()(fakeRequestWithJsonBody(validIndividualWithoutIdRequestJson()))

        status(result) mustBe OK
        val response = contentAsJson(result).as[RegisterWithoutIDResponse]
        response.registerWithoutIDResponse.responseDetail.SAFEID.take(2) mustBe "XE"
      }

      "must return 200 OK with SAFEID for 'W' first-name scenario (empty response)" in {
        val wRequestJson = validIndividualWithoutIdRequestJson("William")

        val result = testController.registerWithoutId()(fakeRequestWithJsonBody(wRequestJson))

        status(result) mustBe OK
        val response = contentAsJson(result).as[RegisterWithoutIDResponse]
        response.registerWithoutIDResponse.responseDetail.SAFEID.take(2) mustBe "XE"
      }

      "must return 400 Bad Request when first name starts with 'Z'" in {
        val zRequestJson = validIndividualWithoutIdRequestJson("Zara")

        val result = testController.registerWithoutId()(fakeRequestWithJsonBody(zRequestJson))

        status(result) mustBe BAD_REQUEST

        val errorResponse = contentAsJson(result).as[ErrorResponse]
        errorResponse.errorDetail.errorCode    mustBe "400"
        errorResponse.errorDetail.errorMessage mustBe "Bad Request"
      }

      "must return a 422 error response when first name starts with 'X'" in {
        val xRequestJson = validIndividualWithoutIdRequestJson("Xavier")

        val result = testController.registerWithoutId()(fakeRequestWithJsonBody(xRequestJson))
        status(result) mustBe UNPROCESSABLE_ENTITY

        val errorResponse = contentAsJson(result).as[ErrorResponse]
        errorResponse.errorDetail.errorCode    mustBe "422"
        errorResponse.errorDetail.errorMessage mustBe "The match was unsuccessful"
      }

      "must return 500 Internal Server Error when first name starts with 'Y'" in {
        val yRequestJson = validIndividualWithoutIdRequestJson("Yolanda")

        val result = testController.registerWithoutId()(fakeRequestWithJsonBody(yRequestJson))
        status(result) mustBe INTERNAL_SERVER_ERROR

        val errorResponse = contentAsJson(result).as[ErrorResponse]
        errorResponse.errorDetail.errorCode    mustBe "500"
        errorResponse.errorDetail.errorMessage mustBe "Unexpected error"
      }

      "must return 400 Bad Request when the request JSON is invalid for RegisterWithoutId" in {
        val result = testController.registerWithoutId()(fakeRequestWithJsonBody(Json.obj("invalid" -> "payload")))
        status(result)        mustBe BAD_REQUEST
        contentAsString(result) must include("Invalid RegisterWithoutIDRequestWrapper payload")
      }

      "must return 503 Service Unavailable when first name starts with 'S'" in {
        val sRequestJson = validIndividualWithoutIdRequestJson("Sarah")

        val result = testController.registerWithoutId()(fakeRequestWithJsonBody(sRequestJson))

        status(result) mustBe SERVICE_UNAVAILABLE

        val errorResponse = contentAsJson(result).as[ErrorResponse]
        errorResponse.errorDetail.errorCode    mustBe "503"
        errorResponse.errorDetail.errorMessage mustBe "Service unavailable"
      }

      "must return 403 Forbidden when first name starts with 'F'" in {
        val fRequestJson = validIndividualWithoutIdRequestJson("Frank")

        val result = testController.registerWithoutId()(fakeRequestWithJsonBody(fRequestJson))

        status(result)          mustBe FORBIDDEN
        contentAsString(result) mustBe "Forbidden"
      }

    }

    "register Organisation Without ID" - {
      "must return 200 OK with SAFEID for a valid request" in {
        val result =
          testController.registerWithoutId()(fakeRequestWithJsonBody(validOrganisationWithoutIdRequestJson()))

        status(result) mustBe OK
        val response = contentAsJson(result).as[RegisterWithoutIDResponse]
        response.registerWithoutIDResponse.responseDetail.SAFEID.take(2) mustBe "XE"
      }

      "must return 200 OK with SAFEID for 'W' first-name scenario (empty response)" in {
        val wRequestJson = validOrganisationWithoutIdRequestJson("William LTD")

        val result = testController.registerWithoutId()(fakeRequestWithJsonBody(wRequestJson))

        status(result) mustBe OK
        val response = contentAsJson(result).as[RegisterWithoutIDResponse]
        response.registerWithoutIDResponse.responseDetail.SAFEID.take(2) mustBe "XE"
      }

      "must return 400 Bad Request when first name starts with 'Z'" in {
        val zRequestJson = validOrganisationWithoutIdRequestJson("Zara LTD")

        val result = testController.registerWithoutId()(fakeRequestWithJsonBody(zRequestJson))

        status(result) mustBe BAD_REQUEST

        val errorResponse = contentAsJson(result).as[ErrorResponse]
        errorResponse.errorDetail.errorCode    mustBe "400"
        errorResponse.errorDetail.errorMessage mustBe "Bad Request"
      }

      "must return a 422 error response when first name starts with 'X'" in {
        val xRequestJson = validOrganisationWithoutIdRequestJson("Xavier LTD")

        val result = testController.registerWithoutId()(fakeRequestWithJsonBody(xRequestJson))
        status(result) mustBe UNPROCESSABLE_ENTITY

        val errorResponse = contentAsJson(result).as[ErrorResponse]
        errorResponse.errorDetail.errorCode    mustBe "422"
        errorResponse.errorDetail.errorMessage mustBe "The match was unsuccessful"
      }

      "must return 500 Internal Server Error when first name starts with 'Y'" in {
        val yRequestJson = validOrganisationWithoutIdRequestJson("Yolanda LTD")

        val result = testController.registerWithoutId()(fakeRequestWithJsonBody(yRequestJson))
        status(result) mustBe INTERNAL_SERVER_ERROR

        val errorResponse = contentAsJson(result).as[ErrorResponse]
        errorResponse.errorDetail.errorCode    mustBe "500"
        errorResponse.errorDetail.errorMessage mustBe "Unexpected error"
      }

      "must return 503 Service Unavailable when first name starts with 'S'" in {
        val sRequestJson = validOrganisationWithoutIdRequestJson("Sarah LTD")

        val result = testController.registerWithoutId()(fakeRequestWithJsonBody(sRequestJson))

        status(result) mustBe SERVICE_UNAVAILABLE

        val errorResponse = contentAsJson(result).as[ErrorResponse]
        errorResponse.errorDetail.errorCode    mustBe "503"
        errorResponse.errorDetail.errorMessage mustBe "Service unavailable"
      }

      "must return 403 Forbidden when first name starts with 'F'" in {
        val fRequestJson = validOrganisationWithoutIdRequestJson("Frank LTD")

        val result = testController.registerWithoutId()(fakeRequestWithJsonBody(fRequestJson))

        status(result)          mustBe FORBIDDEN
        contentAsString(result) mustBe "Forbidden"
      }
    }

    "Other errors" - {

      "must return 400 Bad Request when the request JSON is invalid" in {
        val result = testController.register()(fakeRequestWithJsonBody(Json.obj("invalid" -> "payload")))

        status(result)        mustBe BAD_REQUEST
        contentAsString(result) must include("error.path.missing")
      }

    }
  }

}
