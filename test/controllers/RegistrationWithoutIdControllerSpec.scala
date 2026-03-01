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
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{contentAsJson, contentAsString, status}
import uk.gov.hmrc.carfstubs.controllers.RegistrationWithoutIdStubController
import uk.gov.hmrc.carfstubs.models.request._
import uk.gov.hmrc.carfstubs.models.response._

import java.time.LocalDate

class RegistrationWithoutIdControllerSpec extends SpecBase {

  private val testController: RegistrationWithoutIdStubController = new RegistrationWithoutIdStubController(cc)

  private val validWithoutIdRequest: RegisterWithoutIDRequest =
    RegisterWithoutIDRequest(
      requestCommon = RequestCommon(
        acknowledgementReference = "Test-Ref-WithoutId",
        receiptDate = LocalDate.now().toString,
        regime = "CARF"
      ),
      requestDetail = RequestDetailWithoutId(
        individual = IndividualDetailsWithoutId(
          firstName = "John",
          lastName = "Doe",
          dateOfBirth = "1990-01-01"
        ),
        address = AddressDetails(
          addressLine1 = "10 Test Street",
          addressLine2 = Some("Testington"),
          addressLine3 = None,
          addressLine4 = None,
          postalCode = Some("AB1 2CD"),
          countryCode = "GB"
        ),
        contactDetails = uk.gov.hmrc.carfstubs.models.request.ContactDetails(
          emailAddress = "john.doe@example.com",
          phoneNumber = Some("01234567890")
        ),
        IsAnAgent = false,
        IsAGroup = false
      )
    )

  "RegistrationWithoutIdStubController" - {

    "register Individual (Without ID)" - {

      "must return 200 OK with SAFEID for a valid request" in {
        val result = testController.register()(fakeRequestWithJsonBody(Json.toJson(validWithoutIdRequest)))

        status(result) mustBe OK
        val response = contentAsJson(result).as[RegisterWithoutIDResponse]
        response.responseDetail.SAFEID mustBe "Test-SafeId"
      }

      "must return 200 OK with SAFEID for 'W' first-name scenario (empty response)" in {
        val wRequest =
          validWithoutIdRequest.copy(
            requestDetail = validWithoutIdRequest.requestDetail.copy(
              individual = validWithoutIdRequest.requestDetail.individual.copy(firstName = "William")
            )
          )

        val result = testController.register()(fakeRequestWithJsonBody(Json.toJson(wRequest)))

        status(result) mustBe OK
        val response = contentAsJson(result).as[RegisterWithoutIDResponse]
        response.responseDetail.SAFEID mustBe "Test-SafeId"
      }

      "must return 200 OK with SAFEID for 'Z' first-name scenario (non-UK response)" in {
        val zRequest =
          validWithoutIdRequest.copy(
            requestDetail = validWithoutIdRequest.requestDetail.copy(
              individual = validWithoutIdRequest.requestDetail.individual.copy(firstName = "Zara")
            )
          )

        val result = testController.register()(fakeRequestWithJsonBody(Json.toJson(zRequest)))

        status(result) mustBe OK
        val response = contentAsJson(result).as[RegisterWithoutIDResponse]
        response.responseDetail.SAFEID mustBe "Test-SafeId"
      }

      "must return 404 Not Found when first name starts with 'X'" in {
        val xRequest =
          validWithoutIdRequest.copy(
            requestDetail = validWithoutIdRequest.requestDetail.copy(
              individual = validWithoutIdRequest.requestDetail.individual.copy(firstName = "Xavier")
            )
          )

        val result = testController.register()(fakeRequestWithJsonBody(Json.toJson(xRequest)))
        status(result)        mustBe NOT_FOUND
        contentAsString(result) must include("The match was unsuccessful")
      }

      "must return 500 Internal Server Error when first name starts with 'Y'" in {
        val yRequest =
          validWithoutIdRequest.copy(
            requestDetail = validWithoutIdRequest.requestDetail.copy(
              individual = validWithoutIdRequest.requestDetail.individual.copy(firstName = "Yolanda")
            )
          )

        val result = testController.register()(fakeRequestWithJsonBody(Json.toJson(yRequest)))
        status(result)        mustBe INTERNAL_SERVER_ERROR
        contentAsString(result) must include("Unexpected error")
      }
    }

    "Other errors" - {
      "must return 400 Bad Request when the request JSON is invalid" in {
        val result = testController.register()(fakeRequestWithJsonBody(Json.obj("invalid" -> "payload")))
        status(result)        mustBe BAD_REQUEST
        contentAsString(result) must include("Invalid RegisterWithoutIDRequest payload")
      }
    }
  }
}
