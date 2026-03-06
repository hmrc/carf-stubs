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
import org.scalatest.matchers.must.Matchers
import play.api.http.Status.NO_CONTENT
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.carfstubs.controllers.routes

class EnrolmentControllerSpec extends SpecBase with Matchers {

  "EnrolmentController" - {
    "upsertEnrolment" - {
      s"must return NoContent - $NO_CONTENT for a valid json" in {
        val json: JsValue = Json.parse(
          s"""
             |{
             |  "identifiers": [ { "key": "CARFID", "value": "AA000003D" } ],
             |  "verifiers": [
             |          {
             |              "key": "PostCode",
             |              "value": "N15 2FY"
             |          },
             |          {
             |              "key": "IsAbroad",
             |              "value": "N"
             |          }
             |   ]
             |}
             |""".stripMargin
        )

        val request = FakeRequest(PUT, routes.EnrolmentController.upsertEnrolment.url).withBody(json)
        val result  = route(app, request).value

        status(result) mustBe NO_CONTENT
      }

      s"must return Bad Request - $BAD_REQUEST for a CARFID value starts with (8)" in {
        val json: JsValue = Json.parse(
          s"""
             |{
             |  "identifiers": [ { "key": "CARFID", "value": "8A000008D" } ],
             |  "verifiers": [
             |          {
             |              "key": "PostCode",
             |              "value": "N15 2FY"
             |          },
             |          {
             |              "key": "IsAbroad",
             |              "value": "N"
             |          }
             |   ]
             |}
             |""".stripMargin
        )

        val request = FakeRequest(PUT, routes.EnrolmentController.upsertEnrolment.url).withBody(json)
        val result  = route(app, request).value

        status(result)        mustBe BAD_REQUEST
        contentAsString(result) must include(
          "Provided service name is not in services-to-activate or No group ID in active auth session"
        )
      }
      s"must return Bad Request - $BAD_REQUEST for a CARFID value starts with (X)" in {
        val json: JsValue = Json.parse(
          s"""
             |{
             |  "identifiers": [ { "key": "CARFID", "value": "XA000008D" } ],
             |  "verifiers": [
             |          {
             |              "key": "PostCode",
             |              "value": "N15 2FY"
             |          },
             |          {
             |              "key": "IsAbroad",
             |              "value": "N"
             |          }
             |   ]
             |}
             |""".stripMargin
        )

        val request = FakeRequest(PUT, routes.EnrolmentController.upsertEnrolment.url).withBody(json)
        val result  = route(app, request).value

        status(result)        mustBe BAD_REQUEST
        contentAsString(result) must include(
          "Provided service name is not in services-to-activate or No group ID in active auth session"
        )
      }
      s"must return Bad Request - $BAD_REQUEST for a request with empty identifiers" in {
        val json: JsValue = Json.parse(
          s"""
             |{
             |  "identifiers": [],
             |  "verifiers": [
             |          {
             |              "key": "PostCode",
             |              "value": "N15 2FY"
             |          },
             |          {
             |              "key": "IsAbroad",
             |              "value": "N"
             |          }
             |   ]
             |}
             |""".stripMargin
        )

        val request = FakeRequest(PUT, routes.EnrolmentController.upsertEnrolment.url).withBody(json)
        val result  = route(app, request).value

        status(result)        mustBe BAD_REQUEST
        contentAsString(result) must include(
          "Provided service name is not in services-to-activate or No group ID in active auth session"
        )
      }
      s"must return Bad Request - $INTERNAL_SERVER_ERROR for a CARFID value starts with (9)" in {
        val json: JsValue = Json.parse(
          s"""
             |{
             |  "identifiers": [ { "key": "CARFID", "value": "9A000008D" } ],
             |  "verifiers": [
             |          {
             |              "key": "PostCode",
             |              "value": "N15 2FY"
             |          },
             |          {
             |              "key": "IsAbroad",
             |              "value": "N"
             |          }
             |   ]
             |}
             |""".stripMargin
        )

        val request = FakeRequest(PUT, routes.EnrolmentController.upsertEnrolment.url).withBody(json)
        val result  = route(app, request).value

        status(result)        mustBe INTERNAL_SERVER_ERROR
        contentAsString(result) must include("Internal Server Error")
      }
      s"must return Bad Request - $INTERNAL_SERVER_ERROR for a CARFID value starts with (Y)" in {
        val json: JsValue = Json.parse(
          s"""
             |{
             |  "identifiers": [ { "key": "CARFID", "value": "YA000008D" } ],
             |  "verifiers": [
             |          {
             |              "key": "PostCode",
             |              "value": "N15 2FY"
             |          },
             |          {
             |              "key": "IsAbroad",
             |              "value": "N"
             |          }
             |   ]
             |}
             |""".stripMargin
        )

        val request = FakeRequest(PUT, routes.EnrolmentController.upsertEnrolment.url).withBody(json)
        val result  = route(app, request).value

        status(result)        mustBe INTERNAL_SERVER_ERROR
        contentAsString(result) must include("Internal Server Error")
      }
    }
  }
}
