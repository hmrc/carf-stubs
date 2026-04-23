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

  "SubscriptionController" - {
    "createSubscription" - {

      s"must return Ok - $OK response for a valid json with secondary contact organisation" in {
        val json: JsValue = createSubscriptionSecondaryContactOrgJson("John", "XE000123456792", "Tools for Traders")
        val request       = FakeRequest(POST, routes.SubscriptionController.createSubscription().url).withBody(json)
        val result        = route(app, request).value

        status(result) mustBe OK
      }

      s"must return Ok - $OK response for a valid json with secondary contact individual" in {
        val json: JsValue = createSubscriptionSecondaryContactIndJson("Walker", "XE000123456799")
        val request       = FakeRequest(POST, routes.SubscriptionController.createSubscription().url).withBody(json)
        val result        = route(app, request).value

        status(result) mustBe OK
      }

      "must return 200 with success response for valid individual subscription" in {
        val individual   = Individual("John", "Doe")
        val contact      = Contact("test@example.com", Some(individual), None, Some("1234567890"), Some("1234567890"))
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

        status(result) mustBe OK
        val json = contentAsJson(result)
        (json \ "success" \ "carfReference").as[String]       must startWith("XCARF")
        (json \ "success" \ "processingDate").asOpt[String] mustBe defined

      }

      "return 200 with success response for valid organisation subscription" in {
        val organisation     = Organisation("Test Org Ltd")
        val primaryContact   =
          Contact("primary@example.com", None, Some(organisation), Some("1234567890"), Some("1234567890"))
        val individual       = Individual("Jane", "Smith")
        val secondaryContact = Contact("secondary@example.com", Some(individual), None, None, None)
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

        status(result) mustBe OK
        val json = contentAsJson(result)
        (json \ "success" \ "carfReference").as[String] must startWith("XCARF")
      }

      s"must return Ok - $OK response with a CARFID that will return bad request for enrolment stubs" in {
        val json: JsValue = createSubscriptionSecondaryContactOrgJson("John", "XE000123456792", "Tools for TraderXX")
        val request       = FakeRequest(POST, routes.SubscriptionController.createSubscription().url).withBody(json)
        val result        = route(app, request).value

        status(result) mustBe OK
        val jsonResult = contentAsJson(result)
        (jsonResult \ "success" \ "carfReference").as[String] must startWith("WCARF")
      }

      s"must return Ok - $OK response with a CARFID that will return internal server error for enrolment stubs" in {
        val json: JsValue = createSubscriptionSecondaryContactOrgJson("John", "XE000123456792", "Tools for TraderYY")
        val request       = FakeRequest(POST, routes.SubscriptionController.createSubscription().url).withBody(json)
        val result        = route(app, request).value

        status(result) mustBe OK
        val jsonResult = contentAsJson(result)
        (jsonResult \ "success" \ "carfReference").as[String] must startWith("YCARF")
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

        status(result) mustBe BAD_REQUEST
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

        status(result) mustBe BAD_REQUEST
      }

      "return 400 for invalid JSON" in {
        val request = FakeRequest(POST, routes.SubscriptionController.createSubscription().url)
          .withJsonBody(Json.obj("invalid" -> "data"))

        val result = route(app, request).value

        status(result) mustBe BAD_REQUEST
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

        status(result) mustBe BAD_REQUEST
      }

      "return 400 for missing required fields (idNumber)" in {
        val invalidJson: JsValue = Json.parse("""
        {
          "gbUser": true,
          "idType": "SAFE",
          "primaryContact": {
            "email": "test@example.com",
            "individual": {"firstName": "John", "lastName": "Doe"},
            "phone": "1234567890"
          },
          "secondaryContact": {
            "individual": {"firstName": "Jane", "lastName": "Smith"},
            "phone": "0987654321"
          },
          "tradingName": "Some Trading Name"
        }
      """)

        val request = FakeRequest(POST, routes.SubscriptionController.createSubscription().url)
          .withJsonBody(invalidJson)

        val result = route(app, request).value

        status(result)        mustBe BAD_REQUEST
        contentAsString(result) must include("idNumber")
      }

      "return 422 with error code 008 when firstName is 'noBusinessPartner'" in {
        val individual   = Individual("noBusinessPartner", "Doe")
        val contact      = Contact("test@example.com", Some(individual), None, Some("1234567890"), None)
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

        status(result) mustBe UNPROCESSABLE_ENTITY
        val json = contentAsJson(result)
        (json \ "errorDetail" \ "errorCode").as[String]    mustBe "422"
        (json \ "errorDetail" \ "errorMessage").as[String] mustBe "No Business Partner identified for ID provided"
      }

      "return 422 with error code 004 when firstName is 'duplicateSubmission'" in {
        val individual   = Individual("duplicateSubmission", "Doe")
        val contact      = Contact("test@example.com", Some(individual), None, Some("1234567890"), None)
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

        status(result) mustBe UNPROCESSABLE_ENTITY
        val json = contentAsJson(result)
        (json \ "errorDetail" \ "errorCode").as[String]    mustBe "004"
        (json \ "errorDetail" \ "errorMessage").as[String] mustBe "Duplicate submission acknowledgment reference"
      }

      "return 422 with error code 007 when firstName is 'duplicateAlreadyRegistered'" in {
        val individual   = Individual("duplicateAlreadyRegistered", "Doe")
        val contact      = Contact("test@example.com", Some(individual), None, Some("1234567890"), None)
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

        status(result) mustBe UNPROCESSABLE_ENTITY
        val json = contentAsJson(result)
        (json \ "errorDetail" \ "errorCode").as[String] mustBe "007"
        (json \ "errorDetail" \ "errorMessage")
          .as[String]                                   mustBe "Business Partner already has a Subscription for this regime "
      }

      "return 422 with error code 422 when firstName is 'alreadyRegistered'" in {
        val individual   = Individual("alreadyRegistered", "Doe")
        val contact      = Contact("test@example.com", Some(individual), None, Some("1234567890"), Some("1234567890"))
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

        status(result) mustBe UNPROCESSABLE_ENTITY
        val json = contentAsJson(result)
        (json \ "errorDetail" \ "errorCode").as[String]    mustBe "422"
        (json \ "errorDetail" \ "errorMessage").as[String] mustBe "Business Error (from backend)"
      }

      "return 422 with error code 015 when firstName is 'invalidType'" in {
        val individual   = Individual("invalidType", "Doe")
        val contact      = Contact("test@example.com", Some(individual), None, Some("1234567890"), None)
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

        status(result) mustBe UNPROCESSABLE_ENTITY
        val json = contentAsJson(result)
        (json \ "errorDetail" \ "errorCode").as[String]    mustBe "015"
        (json \ "errorDetail" \ "errorMessage").as[String] mustBe "Invalid ID type"
      }

      "return 400 when firstName is 'badRequest'" in {
        val individual   = Individual("badRequest", "Doe")
        val contact      = Contact("test@example.com", Some(individual), None, Some("1234567890"), None)
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

        status(result) mustBe BAD_REQUEST
        val json = contentAsJson(result)
        (json \ "errorDetail" \ "errorCode").as[String]    mustBe "400"
        (json \ "errorDetail" \ "errorMessage").as[String] mustBe "Bad Request"
      }

      "return 500 when firstName is 'internalServerError'" in {
        val individual   = Individual("internalServerError", "Doe")
        val contact      = Contact("test@example.com", Some(individual), None, Some("1234567890"), None)
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

        status(result) mustBe INTERNAL_SERVER_ERROR
        val json = contentAsJson(result)
        (json \ "errorDetail" \ "errorCode").as[String]    mustBe "500"
        (json \ "errorDetail" \ "errorMessage").as[String] mustBe "Internal Server Error"
      }

      "return 503 when firstName is 'serviceUnavailable'" in {
        val individual   = Individual("serviceUnavailable", "Doe")
        val contact      = Contact("test@example.com", Some(individual), None, Some("1234567890"), None)
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

        status(result) mustBe SERVICE_UNAVAILABLE
        val json = contentAsJson(result)
        (json \ "errorDetail" \ "errorCode").as[String]    mustBe "503"
        (json \ "errorDetail" \ "errorMessage").as[String] mustBe "Service Unavailable"
      }

      "return 500 with error code 003 when firstName is 'invalid'" in {
        val individual   = Individual("invalid", "Doe")
        val contact      = Contact("test@example.com", Some(individual), None, Some("1234567890"), None)
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

        status(result) mustBe INTERNAL_SERVER_ERROR
        val json = contentAsJson(result)
        (json \ "errorDetail" \ "errorCode").as[String]    mustBe "003"
        (json \ "errorDetail" \ "errorMessage").as[String] mustBe "Request could not be processed"
      }
    }

    "displaySubscription" - {

      s"must return Ok - $OK response with full individual response for a valid CARFID" in {
        val request = FakeRequest(GET, routes.SubscriptionController.displaySubscription("CCCAR0024000102").url)
        val result  = route(app, request).value

        status(result) mustBe OK
        val json = contentAsJson(result)
        (json \ "success" \ "carfSubscriptionDetails" \ "carfReference").as[String] must startWith("C")
        (json \ "success" \ "carfSubscriptionDetails" \ "primaryContact" \ "individual")
          .asOpt[Individual]                                                      mustBe defined
      }

      s"must return Ok - $OK response with full organisation response for a valid CARFID starting with R" in {
        val request = FakeRequest(GET, routes.SubscriptionController.displaySubscription("RCCAR0024000102").url)
        val result  = route(app, request).value

        status(result) mustBe OK
        val json = contentAsJson(result)
        (json \ "success" \ "carfSubscriptionDetails" \ "carfReference").as[String] must startWith("R")
        (json \ "success" \ "carfSubscriptionDetails" \ "primaryContact" \ "organisation")
          .asOpt[Organisation]                                                    mustBe defined
      }

      s"must return Ok - $OK response with empty response for a valid CARFID starting with W" in {
        val request = FakeRequest(GET, routes.SubscriptionController.displaySubscription("WCCAR0024000102").url)
        val result  = route(app, request).value

        status(result) mustBe OK
        val json = contentAsJson(result)
        (json \ "success" \ "carfSubscriptionDetails" \ "carfReference").as[String]    must startWith("W")
        (json \ "success" \ "carfSubscriptionDetails" \ "tradingName").asOpt[String] mustBe empty
      }

      s"must return Internal Server Error - $INTERNAL_SERVER_ERROR response for a valid CARFID starting with Y" in {
        val request = FakeRequest(GET, routes.SubscriptionController.displaySubscription("YCCAR0024000102").url)
        val result  = route(app, request).value

        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      s"must return Not Found - $NOT_FOUND response with for a valid CARFID starting with X" in {
        val request = FakeRequest(GET, routes.SubscriptionController.displaySubscription("XCCAR0024000102").url)
        val result  = route(app, request).value

        status(result) mustBe NOT_FOUND
      }

      s"must return Bad Request - $BAD_REQUEST response for a valid CARFID starting with T" in {
        val request = FakeRequest(GET, routes.SubscriptionController.displaySubscription("TCCAR0024000102").url)
        val result  = route(app, request).value

        status(result) mustBe BAD_REQUEST
      }

      s"must return Unprocessable Entity - 422 response for a valid CARFID starting with P" in {
        val request = FakeRequest(GET, routes.SubscriptionController.displaySubscription("PCCAR0024000102").url)
        val result  = route(app, request).value

        status(result) mustBe UNPROCESSABLE_ENTITY
      }

      s"must return Ok - 200 response with no phone for a valid CARFID starting with O" in {
        val request = FakeRequest(GET, routes.SubscriptionController.displaySubscription("OCCAR0024000102").url)
        val result  = route(app, request).value

        status(result) mustBe OK
        val json = contentAsJson(result)
        (json \ "success" \ "carfSubscriptionDetails" \ "carfReference").as[String]                 must startWith("O")
        (json \ "success" \ "carfSubscriptionDetails" \ "primaryContact" \ "phone").asOpt[String] mustBe empty
      }

      s"must return Service Unavailable - $SERVICE_UNAVAILABLE response for a valid CARFID starting with S" in {
        val request = FakeRequest(GET, routes.SubscriptionController.displaySubscription("SCCAR0024000102").url)
        val result  = route(app, request).value

        status(result) mustBe SERVICE_UNAVAILABLE
      }
    }
  }

  private def createSubscriptionSecondaryContactOrgJson(firstName: String, idNumber: String, orgName: String): JsValue =
    Json.parse(
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
       |      "name": "$orgName"
       |    },
       |    "email": "contact@toolsfortraders.com",
       |    "phone": "+44 020 39898980"
       | }
       |}
       |""".stripMargin
    )

  private def createSubscriptionSecondaryContactIndJson(firstName: String, idNumber: String): JsValue = Json.parse(
    s"""
       |{
       |  "gbUser": false,
       |  "idNumber": "$idNumber",
       |  "idType": "SAFE",
       |  "primaryContact": {
       |    "email": "mj@gmailqqq.com",
       |    "individual": {
       |      "firstName": "$firstName",
       |      "lastName": "Lname1",
       |      "middleName": "lxtt"
       |    },
       |    "mobile": "7834512345",
       |    "phone": "+44-7865412345"
       |  },
       |  "secondaryContact": {
       |    "email": "djwkxescl@gmail.com",
       |    "individual": {
       |      "firstName": "name2",
       |      "lastName": "Lname2",
       |      "middleName": "bp"
       |    },
       |    "mobile": "7834512345",
       |    "phone": "+44-7865412345"
       |  },
       |  "tradingName": "ABC Trader"
       |}
       |""".stripMargin
  )

}
