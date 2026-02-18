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
import org.scalatest.OptionValues
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.carfstubs.controllers.routes
import uk.gov.hmrc.carfstubs.models.request.{Contact, Subscription}
import uk.gov.hmrc.carfstubs.models.{Individual, Organisation}

class SubscriptionControllerSpec extends SpecBase with OptionValues {
  import org.scalatest.matchers.should.Matchers.*

  "SubscriptionController" - {
    "createSubscription" - {

      s"must return Created - $CREATED response for the valid input request" in {
        val json: JsValue = createSubscriptionJson("John", "XWG456789")
        val request       = FakeRequest(POST, routes.SubscriptionController.createSubscription().url).withBody(json)
        val result        = route(app, request).value

        status(result) shouldBe CREATED
      }

      "must return 201 with success response for valid individual subscription" in {
        val individual   = Individual("John", "Doe")
        val contact      = Contact("test@example.com", Some(individual), None, Some("1234567890"))
        val subscription = Subscription(
          gbUser = true,
          idNumber = "XWG456789",
          idType = "SAFE",
          primaryContact = contact,
          secondaryContact = None,
          tradingName = None
        )

        val request = FakeRequest(POST, routes.SubscriptionController.createSubscription().url)
          .withJsonBody(Json.toJson(subscription))

        val result = route(app, request).value

        status(result) shouldBe CREATED
        val json = contentAsJson(result)
        (json \ "success" \ "CARFReference").as[String]       should startWith("XCARF")
        (json \ "success" \ "processingDate").asOpt[String] shouldBe defined

      }

      "return 201 with success response for valid organisation subscription" in {
        val organisation     = Organisation("Test Org Ltd")
        val primaryContact   = Contact("primary@example.com", None, Some(organisation), Some("1234567890"))
        val individual       = Individual("Jane", "Smith")
        val secondaryContact = Contact("secondary@example.com", Some(individual), None, None)
        val subscription     = Subscription(
          gbUser = false,
          idNumber = "XM0321456",
          idType = "SAFE",
          primaryContact = primaryContact,
          secondaryContact = Some(secondaryContact),
          tradingName = Some("Trading Name")
        )

        val request = FakeRequest(POST, routes.SubscriptionController.createSubscription().url)
          .withJsonBody(Json.toJson(subscription))

        val result = route(app, request).value

        status(result) shouldBe CREATED
        val json = contentAsJson(result)
        (json \ "success" \ "CARFReference").as[String] should startWith("XCARF")
      }

      "return 400 for contact with both individual and organisation" in {
        val invalidJson: JsValue = Json.parse("""{
          "gbUser": true,
          "idNumber": "SAFE123456",
          "idType": "SAFE",
          "primaryContact": {
            "email": "test@example.com",
            "individual": {"firstName": "John", "lastName": "Doe"},
            "organisation": {"name": "Test Org"},
            "phone": "1234567890"
          },
          "secondaryContact": null,
          "tradingName": null
        }""")

        val request = FakeRequest(POST, routes.SubscriptionController.createSubscription().url)
          .withJsonBody(invalidJson)

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST
      }

      "return 400 for contact with neither individual nor organisation" in {
        val invalidJson: JsValue = Json.parse("""
        {
          "gbUser": true,
          "idNumber": "SAFE123456",
          "idType": "SAFE",
          "primaryContact": {
            "email": "test@example.com",
            "phone": "1234567890"
          }
        }
      """)

        val request = FakeRequest(POST, routes.SubscriptionController.createSubscription().url)
          .withJsonBody(invalidJson)

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST
      }

      "return 400 for invalid JSON" in {
        val request = FakeRequest(POST, routes.SubscriptionController.createSubscription().url)
          .withJsonBody(Json.obj("invalid" -> "data"))

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST
      }

      "return 400 for missing required fields" in {
        val invalidJson: JsValue = Json.parse("""
        {
          "gbUser": true,
          "idType": "SAFE"
        }
      """)

        val request = FakeRequest(POST, routes.SubscriptionController.createSubscription().url)
          .withJsonBody(invalidJson)

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST
      }

      "return 422 with error code 008 when idNumber starts with XE3" in {
        val individual   = Individual("John", "Doe")
        val contact      = Contact("test@example.com", Some(individual), None, Some("1234567890"))
        val subscription = Subscription(
          gbUser = true,
          idNumber = "XE3456789",
          idType = "SAFE",
          primaryContact = contact,
          secondaryContact = None,
          tradingName = None
        )

        val request = FakeRequest(POST, routes.SubscriptionController.createSubscription().url)
          .withJsonBody(Json.toJson(subscription))

        val result = route(app, request).value

        status(result) shouldBe UNPROCESSABLE_ENTITY
        val json = contentAsJson(result)
        (json \ "errorDetail" \ "errorCode").as[String]    shouldBe "422"
        (json \ "errorDetail" \ "errorMessage").as[String] shouldBe "No Business Partner identified for ID provided"
      }

      "return 422 with error code 004 when firstName is 'duplicate'" in {
        val individual   = Individual("duplicate", "Doe")
        val contact      = Contact("test@example.com", Some(individual), None, Some("1234567890"))
        val subscription = Subscription(
          gbUser = true,
          idNumber = "SAFE123456",
          idType = "SAFE",
          primaryContact = contact,
          secondaryContact = None,
          tradingName = None
        )

        val request = FakeRequest(POST, routes.SubscriptionController.createSubscription().url)
          .withJsonBody(Json.toJson(subscription))

        val result = route(app, request).value

        status(result) shouldBe UNPROCESSABLE_ENTITY
        val json = contentAsJson(result)
        (json \ "errorDetail" \ "errorCode").as[String]    shouldBe "004"
        (json \ "errorDetail" \ "errorMessage").as[String] shouldBe "Duplicate submission acknowledgment reference"
      }

      "return 422 with error code 422 when firstName is 'alreadyRegistered'" in {
        val individual   = Individual("alreadyRegistered", "Doe")
        val contact      = Contact("test@example.com", Some(individual), None, Some("1234567890"))
        val subscription = Subscription(
          gbUser = true,
          idNumber = "SAFE123456",
          idType = "SAFE",
          primaryContact = contact,
          secondaryContact = None,
          tradingName = None
        )

        val request = FakeRequest(POST, routes.SubscriptionController.createSubscription().url)
          .withJsonBody(Json.toJson(subscription))

        val result = route(app, request).value

        status(result) shouldBe UNPROCESSABLE_ENTITY
        val json = contentAsJson(result)
        (json \ "errorDetail" \ "errorCode").as[String]    shouldBe "422"
        (json \ "errorDetail" \ "errorMessage").as[String] shouldBe "Business Error (from backend)"
      }

      "return 422 with error code 015 when idNumber starts with XID" in {
        val individual   = Individual("John", "Doe")
        val contact      = Contact("test@example.com", Some(individual), None, Some("1234567890"))
        val subscription = Subscription(
          gbUser = true,
          idNumber = "XID456789",
          idType = "SAFE",
          primaryContact = contact,
          secondaryContact = None,
          tradingName = None
        )

        val request = FakeRequest(POST, routes.SubscriptionController.createSubscription().url)
          .withJsonBody(Json.toJson(subscription))

        val result = route(app, request).value

        status(result) shouldBe UNPROCESSABLE_ENTITY
        val json = contentAsJson(result)
        (json \ "errorDetail" \ "errorCode").as[String]    shouldBe "015"
        (json \ "errorDetail" \ "errorMessage").as[String] shouldBe "Invalid ID type"
      }

      "return 422 with error code 003 when firstName is 'invalid'" in {
        val individual   = Individual("invalid", "Doe")
        val contact      = Contact("test@example.com", Some(individual), None, Some("1234567890"))
        val subscription = Subscription(
          gbUser = true,
          idNumber = "SAFE123456",
          idType = "SAFE",
          primaryContact = contact,
          secondaryContact = None,
          tradingName = None
        )

        val request = FakeRequest(POST, routes.SubscriptionController.createSubscription().url)
          .withJsonBody(Json.toJson(subscription))

        val result = route(app, request).value

        status(result) shouldBe UNPROCESSABLE_ENTITY
        val json = contentAsJson(result)
        (json \ "errorDetail" \ "errorCode").as[String]    shouldBe "003"
        (json \ "errorDetail" \ "errorMessage").as[String] shouldBe "Request could not be processed"
      }
    }
  }

  private def createSubscriptionJson(firstName: String, idNumber: String): JsValue = Json.parse(
    s"""
       |{
       | "idType": "SAFE",
       | "idNumber": "$idNumber",
       | "tradingName": "Tools for Traders Limited",
       | "gbUser": true,
       | "primaryContact": {
       |    "individual": {
       |      "firstName": "$firstName",
       |      "lastName": "Smith"
       |    },
       |    "email": "john@toolsfortraders.com",
       |    "phone": "0188899999",
       |    "mobile": "07321012345"
       | },
       | "secondaryContact": {
       |    "organisation": {
       |      "name": "Tools for Traders"
       |    },
       |    "email": "contact@toolsfortraders.com",
       |    "phone": "+44 020 39898980"
       | }
       |}
       |""".stripMargin
  )

}
