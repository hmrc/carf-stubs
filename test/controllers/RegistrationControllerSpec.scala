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

class RegistrationControllerSpec extends SpecBase {

  val testController: RegistrationController = new RegistrationController(cc)

  val testIndividualRequestModel: RegisterWithIDRequest =
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
            dateOfBirth = "Test-DOB"
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

  "RegistrationController" - {

    "register Individual" - {

      "must return a 200 OK with a full individual response for a valid NINO" in {
        val originalJson  = Json.toJson(testIndividualRequestModel).as[JsObject]
        val requestDetail = originalJson("requestDetail")
          .as[JsObject] + ("organisation" -> Json.toJson(None: Option[OrganisationDetails]))
        val jsonRequest = originalJson + ("requestDetail" -> requestDetail)

        val result = testController.register()(fakeRequestWithJsonBody(jsonRequest))

        status(result)        mustBe OK
        contentAsString(result) must include("Test-SafeId")
        contentAsString(result) must include("Professor")
      }

      "must return a 200 OK with an empty individual response when the NINO starts with a 7" in {
        val requestWith7  = testIndividualRequestModel.copy(requestDetail =
          testIndividualRequestModel.requestDetail.copy(IDNumber = "7123456A")
        )
        val originalJson  = Json.toJson(requestWith7).as[JsObject]
        val requestDetail = originalJson("requestDetail")
          .as[JsObject] + ("organisation" -> Json.toJson(None: Option[OrganisationDetails]))
        val jsonRequest = originalJson + ("requestDetail" -> requestDetail)

        val result = testController.register()(fakeRequestWithJsonBody(jsonRequest))

        status(result)        mustBe OK
        contentAsString(result) must include("Test-SafeId")
        contentAsString(result) must not include "Birmingham"
      }

      "must return a not found response when the request IDNumber starts with an 8" in {
        val result = testController.register()(
          fakeRequestWithJsonBody(
            Json.toJson(
              testIndividualRequestModel.copy(requestDetail =
                testIndividualRequestModel.requestDetail.copy(IDNumber = "8123")
              )
            )
          )
        )

        status(result)          mustBe NOT_FOUND
        contentAsString(result) mustBe "The match was unsuccessful"
      }

      "register Organisation" - {

        "must return a 200 OK with a full organisation response for a valid UTR" in {
          val result = testController.register()(fakeRequestWithJsonBody(Json.toJson(testOrganisationRequestModel)))

          status(result)        mustBe OK
          contentAsString(result) must include("Test-SafeId")
          contentAsString(result) must include("The Secret Lab Ltd")
          contentAsString(result) must include("0003")
          val responseJson = contentAsJson(result)
          (responseJson \ "responseCommon" \ "status").as[String] mustBe "OK"
          (responseJson \ "responseDetail" \ "SAFEID").as[String] mustBe "Test-SafeId"
          (responseJson \ "responseDetail" \ "organisation" \ "organisationName")
            .as[String]                                           mustBe "The Secret Lab Ltd"
          (responseJson \ "responseDetail" \ "organisation" \ "organisationType")
            .as[String]                                           mustBe "0003"
        }

        "must return a 200 OK with a non-UK organisation response when the UTR starts with a 6" in {
          val request = testOrganisationRequestModel.copy(requestDetail =
            testOrganisationRequestModel.requestDetail.copy(IDNumber = "6123456789")
          )
          val result  = testController.register()(fakeRequestWithJsonBody(Json.toJson(request)))

          status(result)        mustBe OK
          contentAsString(result) must include("The Secret Lab Ltd")
          contentAsString(result) must include("US")

          val responseJson = contentAsJson(result)
          (responseJson \ "responseCommon" \ "status").as[String] mustBe "OK"
          (responseJson \ "responseDetail" \ "SAFEID").as[String] mustBe "Test-SafeId"
          (responseJson \ "responseDetail" \ "organisation" \ "organisationName")
            .as[String]                                           mustBe "The Secret Lab Ltd"
          (responseJson \ "responseDetail" \ "organisation" \ "organisationType")
            .as[String]                                           mustBe "0003"
          (responseJson \ "responseDetail" \ "address" \ "countryCode")
            .as[String]                                           mustBe "US"
        }

        "must return a 200 OK with an empty organisation response when the UTR starts with a 7" in {
          val request = testOrganisationRequestModel.copy(requestDetail =
            testOrganisationRequestModel.requestDetail.copy(IDNumber = "7123456789")
          )
          val result  = testController.register()(fakeRequestWithJsonBody(Json.toJson(request)))

          status(result)        mustBe OK
          contentAsString(result) must include("The Secret Lab Ltd")
          contentAsString(result) must not include "Birmingham"
        }
      }

      "register Errors" - {

        "must return 404 Not Found for an Individual when the NINO starts with an 8" in {
          val request = testIndividualRequestModel.copy(requestDetail =
            testIndividualRequestModel.requestDetail.copy(IDNumber = "8123456A")
          )
          val result  = testController.register()(fakeRequestWithJsonBody(Json.toJson(request)))

          status(result)          mustBe NOT_FOUND
          contentAsString(result) mustBe "The match was unsuccessful"
        }

        "must return 500 Internal Server Error for an Individual when the NINO starts with a 9" in {
          val request = testIndividualRequestModel.copy(requestDetail =
            testIndividualRequestModel.requestDetail.copy(IDNumber = "9123456A")
          )
          val result  = testController.register()(fakeRequestWithJsonBody(Json.toJson(request)))

          status(result)          mustBe INTERNAL_SERVER_ERROR
          contentAsString(result) mustBe "Unexpected error"
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

          status(result)          mustBe INTERNAL_SERVER_ERROR
          contentAsString(result) mustBe "Unexpected error"
        }

        "must return 400 Bad Request when the request JSON is invalid" in {
          val result = testController.register()(fakeRequestWithJsonBody(Json.obj("invalid" -> "payload")))

          status(result)        mustBe BAD_REQUEST
          contentAsString(result) must include("Invalid RegisterWithIDRequest payload")
        }

        "must return 400 Bad Request when the IDType is invalid" in {
          val request = testIndividualRequestModel.copy(requestDetail =
            testIndividualRequestModel.requestDetail.copy(IDType = "INVALID_TYPE")
          )
          val result  = testController.register()(fakeRequestWithJsonBody(Json.toJson(request)))

          status(result)          mustBe BAD_REQUEST
          contentAsString(result) mustBe "Invalid IDType: INVALID_TYPE"
        }
      }
    }
  }
}
