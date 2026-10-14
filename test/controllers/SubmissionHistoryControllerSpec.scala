/*
 * Copyright 2026 HM Revenue & Customs
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
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.carfstubs.controllers.routes
import uk.gov.hmrc.carfstubs.models.response.*

import java.time.{Clock, Instant}

class SubmissionHistoryControllerSpec extends SpecBase {

  private val submissionHistoryResponseCommon =
    SubmissionHistoryResponseCommon(
      regime = "CARF",
      responseParameters = None
    )

  private def application(): Application =
    new GuiceApplicationBuilder()
      .overrides(bind[Clock].toInstance(clock))
      .build()

  "SubmissionHistoryController" - {
    ".getSubmissionHistory" - {
      "must return 200 for a valid json when carfId has second last digit 1" in {
        val json: JsValue = buildSubmissionHistoryRequestJson("XCARF000000010")
        val request       = FakeRequest(POST, routes.SubmissionHistoryController.getSubmissionHistory.url).withBody(json)
        val result        = route(application(), request).value

        val expectedResponse = SubmissionHistoryResponse(
          submissionsListResponse = SubmissionsListResponse(
            responseCommon = submissionHistoryResponseCommon,
            responseDetails = SubmissionHistoryResponseDetails(
              submissionsList = SubmissionHistoryTestData.submissionsListFewRecords(Instant.now(clock))
            )
          )
        )

        status(result)        mustBe OK
        contentAsJson(result) mustBe Json.toJson(expectedResponse)
      }

      "must return 200 for a valid json when carfId has second last digit 2" in {
        val json: JsValue = buildSubmissionHistoryRequestJson("XCARF000000020")
        val request       = FakeRequest(POST, routes.SubmissionHistoryController.getSubmissionHistory.url).withBody(json)
        val result        = route(application(), request).value

        val expectedResponse = SubmissionHistoryResponse(
          submissionsListResponse = SubmissionsListResponse(
            responseCommon = submissionHistoryResponseCommon,
            responseDetails = SubmissionHistoryResponseDetails(
              submissionsList = SubmissionHistoryTestData.submissionsList70Records(Instant.now(clock))
            )
          )
        )

        status(result)        mustBe OK
        contentAsJson(result) mustBe Json.toJson(expectedResponse)
      }

      "must return 200 for a valid json when carfId has second last digit 3" in {
        val json: JsValue = buildSubmissionHistoryRequestJson("XCARF000000030")
        val request       = FakeRequest(POST, routes.SubmissionHistoryController.getSubmissionHistory.url).withBody(json)
        val result        = route(application(), request).value

        val expectedResponse = SubmissionHistoryResponse(
          submissionsListResponse = SubmissionsListResponse(
            responseCommon = submissionHistoryResponseCommon,
            responseDetails = SubmissionHistoryResponseDetails(
              submissionsList = SubmissionHistoryTestData.submissionsList1000Records(Instant.now(clock))
            )
          )
        )

        status(result)        mustBe OK
        contentAsJson(result) mustBe Json.toJson(expectedResponse)
      }

      "must return 422 indicating no records when carfId has second last digit 0" in {
        val json: JsValue = buildSubmissionHistoryRequestJson("XCARF000000000")
        val request       = FakeRequest(POST, routes.SubmissionHistoryController.getSubmissionHistory.url).withBody(json)
        val result        = route(app, request).value

        status(result) mustBe UNPROCESSABLE_ENTITY
        (contentAsJson(result) \ "errorDetail" \ "sourceFaultDetail" \ "detail")
          .as[List[String]]
          .head        mustBe "001 - No matching records found for the request"
      }

      "must return 500 when carfId has second last digit 9" in {
        val json: JsValue = buildSubmissionHistoryRequestJson("XCARF000000090")
        val request       = FakeRequest(POST, routes.SubmissionHistoryController.getSubmissionHistory.url).withBody(json)
        val result        = route(app, request).value

        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      "must return 400 when carfId has second last digit 8" in {
        val json: JsValue = buildSubmissionHistoryRequestJson("XCARF000000080")
        val request       = FakeRequest(POST, routes.SubmissionHistoryController.getSubmissionHistory.url).withBody(json)
        val result        = route(app, request).value

        status(result) mustBe BAD_REQUEST
      }

      "must return 503 when carfId has second last digit 7" in {
        val json: JsValue = buildSubmissionHistoryRequestJson("XCARF000000070")
        val request       = FakeRequest(POST, routes.SubmissionHistoryController.getSubmissionHistory.url).withBody(json)
        val result        = route(app, request).value

        status(result) mustBe SERVICE_UNAVAILABLE
      }

      "must return 422 indicating an error when carfId has second last digit 6" in {
        val json: JsValue = buildSubmissionHistoryRequestJson("XCARF000000060")
        val request       = FakeRequest(POST, routes.SubmissionHistoryController.getSubmissionHistory.url).withBody(json)
        val result        = route(app, request).value

        status(result) mustBe UNPROCESSABLE_ENTITY
        (contentAsJson(result) \ "errorDetail" \ "sourceFaultDetail" \ "detail")
          .as[List[String]]
          .head        mustBe "999 - Unprocessable Entity"
      }

      "must return 405 when carfId has second last digit 5" in {
        val json: JsValue = buildSubmissionHistoryRequestJson("XCARF000000050")
        val request       = FakeRequest(POST, routes.SubmissionHistoryController.getSubmissionHistory.url).withBody(json)
        val result        = route(app, request).value

        status(result) mustBe METHOD_NOT_ALLOWED
      }

      "must return 403 when carfId has second last digit 4" in {
        val json: JsValue = buildSubmissionHistoryRequestJson("XCARF000000040")
        val request       = FakeRequest(POST, routes.SubmissionHistoryController.getSubmissionHistory.url).withBody(json)
        val result        = route(app, request).value

        status(result) mustBe FORBIDDEN
      }

      "must return 400 when the request body is invalid" in {
        val request = FakeRequest(POST, routes.SubmissionHistoryController.getSubmissionHistory.url)
          .withJsonBody(Json.obj("invalid" -> "data"))
        val result  = route(app, request).value

        status(result)        mustBe BAD_REQUEST
        contentAsString(result) must include("Invalid request payload")
      }
    }
  }

  private def buildSubmissionHistoryRequestJson(carfId: String): JsValue = Json.parse(
    s"""{
       |  "submissionsListRequest": {
       |    "requestCommon": {
       |      "originatingSystem": "MDTP",
       |      "transmittingSystem": "CADX",
       |      "regime": "CARF",
       |      "requestParameters": [
       |        {
       |          "paramName": "paramName1",
       |          "paramValue": "paramValue1"
       |        }
       |      ]
       |    },
       |    "requestDetails": {
       |      "subscriptionId": "$carfId"
       |    }
       |  }
       |}
       |""".stripMargin
  )
}
