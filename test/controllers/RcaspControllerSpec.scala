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
import play.api.http.Status.{BAD_REQUEST, FORBIDDEN, INTERNAL_SERVER_ERROR, METHOD_NOT_ALLOWED, SERVICE_UNAVAILABLE}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Results.BadRequest
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.carfstubs.controllers.routes
import uk.gov.hmrc.carfstubs.models.request.{CreateRCASPRequest, RCASPManagementRequest, RcaspCreateRequestCommon, RequestParameter}
import uk.gov.hmrc.carfstubs.models.*
import uk.gov.hmrc.carfstubs.models.response.RcaspDetails

class RcaspControllerSpec extends SpecBase with OptionValues {

  "RcaspController" - {
    "viewRcasp" - {

      s"must return Ok - $OK response with full individual response for a valid CARFID" in {
        val request = FakeRequest(GET, routes.RcaspController.viewRcasp("CCCAR0024000102", "683373339").url)
        val result  = route(app, request).value

        status(result) mustBe OK
        val rcaspDetails = (contentAsJson(result) \ "ViewRCASP" \ "ResponseDetails" \ "RCASPList")
          .as[List[response.IndividualRcaspDetails]]
          .head
        rcaspDetails.SubscriptionID          must startWith("C")
        rcaspDetails.RCASPID                 must startWith("6")
        rcaspDetails.PartyType             mustBe "Individual"
        rcaspDetails.PrimaryContactDetails mustBe defined
      }

      s"must return Ok - $OK response with full organisation response for a valid CARFID starting with RR" in {
        val request = FakeRequest(GET, routes.RcaspController.viewRcasp("RRCAR0024000102", "683373339").url)
        val result  = route(app, request).value

        status(result) mustBe OK
        val rcaspDetails = (contentAsJson(result) \ "ViewRCASP" \ "ResponseDetails" \ "RCASPList")
          .as[List[response.OrganisationRcaspDetails]]
          .head
        rcaspDetails.SubscriptionID            must startWith("R")
        rcaspDetails.RCASPID                   must startWith("6")
        rcaspDetails.PartyType               mustBe "Organisation"
        rcaspDetails.PrimaryContactDetails   mustBe defined
        rcaspDetails.SecondaryContactDetails mustBe defined
      }

      s"must return Ok - $OK response with empty optional fields in individual response for a valid CARFID starting with MM" in {
        val request = FakeRequest(GET, routes.RcaspController.viewRcasp("MMCAR0024000102", "683373339").url)
        val result  = route(app, request).value

        status(result) mustBe OK
        val rcaspDetails = (contentAsJson(result) \ "ViewRCASP" \ "ResponseDetails" \ "RCASPList")
          .as[List[response.IndividualRcaspDetails]]
          .head
        rcaspDetails.SubscriptionID          must startWith("M")
        rcaspDetails.RCASPID                 must startWith("6")
        rcaspDetails.PartyType             mustBe "Individual"
        rcaspDetails.PrimaryContactDetails mustBe empty
      }

      s"must return Ok - $OK response with empty optional fields in organisation response for a valid CARFID starting with OO" in {
        val request = FakeRequest(GET, routes.RcaspController.viewRcasp("OOCAR0024000102", "683373339").url)
        val result  = route(app, request).value

        status(result) mustBe OK
        val rcaspDetails = (contentAsJson(result) \ "ViewRCASP" \ "ResponseDetails" \ "RCASPList")
          .as[List[response.OrganisationRcaspDetails]]
          .head
        rcaspDetails.SubscriptionID            must startWith("O")
        rcaspDetails.RCASPID                   must startWith("6")
        rcaspDetails.PartyType               mustBe "Organisation"
        rcaspDetails.PrimaryContactDetails   mustBe empty
        rcaspDetails.SecondaryContactDetails mustBe empty
      }

      s"must return Ok - $OK response with multiple RCASP items in individual response for a valid CARFID starting with LL" in {
        val request = FakeRequest(GET, routes.RcaspController.viewRcasp("LLCAR0024000102", "683373339").url)
        val result  = route(app, request).value

        status(result) mustBe OK
        (contentAsJson(result) \ "ViewRCASP" \ "ResponseDetails" \ "RCASPList")
          .as[List[response.IndividualRcaspDetails]]
          .length      mustBe 2
      }

      s"must return Ok - $OK response with multiple RCASP items in organisation response for a valid CARFID starting with NN" in {
        val request = FakeRequest(GET, routes.RcaspController.viewRcasp("NNCAR0024000102", "683373339").url)
        val result  = route(app, request).value

        status(result) mustBe OK
        (contentAsJson(result) \ "ViewRCASP" \ "ResponseDetails" \ "RCASPList")
          .as[List[response.OrganisationRcaspDetails]]
          .length      mustBe 2
      }

      s"must return Ok - $OK response with no RCASP items in response for a valid CARFID starting with KK" in {
        val request = FakeRequest(GET, routes.RcaspController.viewRcasp("KKCAR0024000102", "683373339").url)
        val result  = route(app, request).value

        status(result) mustBe OK
        (contentAsJson(result) \ "ViewRCASP" \ "ResponseDetails" \ "RCASPList")
          .as[List[RcaspDetails]]
          .length      mustBe 0
      }

      s"must return Internal Server Error - $INTERNAL_SERVER_ERROR response for a valid CARFID starting with YY" in {
        val request = FakeRequest(GET, routes.RcaspController.viewRcasp("YYCAR0024000102", "683373339").url)
        val result  = route(app, request).value

        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      s"must return Bad Request - $BAD_REQUEST response for a valid CARFID starting with TT" in {
        val request = FakeRequest(GET, routes.RcaspController.viewRcasp("TTCAR0024000102", "683373339").url)
        val result  = route(app, request).value

        status(result) mustBe BAD_REQUEST
      }

      s"must return Unprocessable Entity - 422 response for a valid CARFID starting with PP" in {
        val request = FakeRequest(GET, routes.RcaspController.viewRcasp("PPCAR0024000102", "683373339").url)
        val result  = route(app, request).value

        status(result) mustBe UNPROCESSABLE_ENTITY
      }

      s"must return Service Unavailable - $SERVICE_UNAVAILABLE response for a valid CARFID starting with SS" in {
        val request = FakeRequest(GET, routes.RcaspController.viewRcasp("SSCAR0024000102", "683373339").url)
        val result  = route(app, request).value

        status(result) mustBe SERVICE_UNAVAILABLE
      }
    }

    "createRcasp" - {
      s"must return Ok - $OK response for a valid json with organisation in request" in {
        val json: JsValue = buildCreateOrgRcaspJson("contact@toolsfortraders.com")
        val request       = FakeRequest(POST, routes.RcaspController.createRcasp.url).withBody(json)
        val result        = route(app, request).value

        status(result) mustBe OK
      }

      s"must return Ok - $OK response for a valid json with individual in request" in {
        val json: JsValue = buildCreateIndvRcaspJson("jdoe@example.com")
        val fakeRequest   = FakeRequest(POST, routes.RcaspController.createRcasp.url).withBody(json)
        val result        = route(app, fakeRequest).value

        status(result) mustBe OK
      }

      s"must return Ok - $OK response for a valid json from model" in {
        val json: JsValue = Json.toJson(
          CreateRCASPRequest(
            RCASPManagementRequest(
              RcaspCreateRequestCommon(
                OriginatingSystem = "CADX",
                TransmittingSystem = "EIS",
                RequestType = "VIEW",
                Regime = "CARF",
                RequestParameters = List(RequestParameter("key", "value"))
              ),
              request.IndividualRcaspDetails(
                SubscriptionID = "XCARF000000001",
                IsRCASPUser = true,
                PartyType = "Individual",
                FirstName = "Penny",
                LastName = "Cassiopeia",
                TINDetails = Some(
                  List(
                    TinDetails(
                      TINType = "UTR",
                      TIN = "6893649",
                      IssuedBy = "GB"
                    )
                  )
                ),
                AddressDetails = RcaspAddress(
                  AddressLine1 = "2 High Street",
                  AddressLine2 = Some("Birmingham"),
                  AddressLine3 = Some("Nowhereshire"),
                  AddressLine4 = Some("Down the road"),
                  PostalCode = "B23 2AZ",
                  CountryCode = "GB"
                ),
                PrimaryContactDetails = Some(
                  RcaspContactDetails(
                    ContactName = "Penny Cassiopeia",
                    EmailAddress = "penny.cassiopeia@uva.edu.org",
                    PhoneNumber = Some("07123412345")
                  )
                )
              )
            )
          )
        )

        val fakeRequest = FakeRequest(POST, routes.RcaspController.createRcasp.url).withBody(json)
        val result      = route(app, fakeRequest).value

        status(result) mustBe OK
      }

      s"must return BadRequest - $BadRequest response for a json missing primary contact information" in {
        val json: JsValue = Json.parse(
          s"""
             |{
             |  "RCASPManagement": {
             |    "RequestCommon": {
             |      "OriginatingSystem": "MDTP",
             |      "TransmittingSystem": "EIS",
             |      "RequestType": "UPDATE",
             |      "Regime": "CARF",
             |      "RequestParameters": [
             |        {
             |          "ParamName": "Test",
             |          "ParamValue": "Test"
             |        }
             |      ]
             |    },
             |    "RequestDetails": {
             |      "RCASPID": "683373339",
             |      "FirstName": "John",
             |      "LastName": "Smith",
             |      "IsRCASPUser": false,
             |      "SubscriptionID": "345567808",
             |      "PartyType": "Individual",
             |      "TINDetails": [
             |        {
             |          "TINType": "OTHER",
             |          "TIN": "68936493",
             |          "IssuedBy": "GB"
             |        }
             |      ],
             |      "AddressDetails": {
             |        "AddressLine1": "22",
             |        "AddressLine2": "High Street",
             |        "AddressLine3": "Dawley",
             |        "AddressLine4": "Dawley",
             |        "CountryCode": "GB",
             |        "PostalCode": "TF22 2RE"
             |      }
             |    }
             |  }
             |}
             |
             |""".stripMargin
        )
        val fakeRequest   = FakeRequest(POST, routes.RcaspController.createRcasp.url).withBody(json)
        val result        = route(app, fakeRequest).value

        status(result) mustBe BAD_REQUEST
      }

      s"must return BadRequest - $BAD_REQUEST response for an invalid json" in {
        val json: JsValue = buildCreateIndvRcaspJson("XX@example.com")
        val fakeRequest   = FakeRequest(POST, routes.RcaspController.createRcasp.url).withBody(json)
        val result        = route(app, fakeRequest).value

        status(result) mustBe BAD_REQUEST
      }

      s"must return UnprocessableEntity - $UNPROCESSABLE_ENTITY response for an invalid json" in {
        val json: JsValue = buildCreateIndvRcaspJson("UU@example.com")
        val fakeRequest   = FakeRequest(POST, routes.RcaspController.createRcasp.url).withBody(json)
        val result        = route(app, fakeRequest).value

        status(result) mustBe UNPROCESSABLE_ENTITY
      }

      s"must return Forbidden - $FORBIDDEN response for an invalid json" in {
        val json: JsValue = buildCreateIndvRcaspJson("VV@example.com")
        val fakeRequest   = FakeRequest(POST, routes.RcaspController.createRcasp.url).withBody(json)
        val result        = route(app, fakeRequest).value

        status(result) mustBe FORBIDDEN
      }

      s"must return MethodNotAllowed - $METHOD_NOT_ALLOWED response for an invalid json" in {
        val json: JsValue = buildCreateIndvRcaspJson("WW@example.com")
        val fakeRequest   = FakeRequest(POST, routes.RcaspController.createRcasp.url).withBody(json)
        val result        = route(app, fakeRequest).value

        status(result) mustBe METHOD_NOT_ALLOWED
      }

      s"must return InternalServerError - $INTERNAL_SERVER_ERROR response for an invalid json" in {
        val json: JsValue = buildCreateIndvRcaspJson("YY@example.com")
        val fakeRequest   = FakeRequest(POST, routes.RcaspController.createRcasp.url).withBody(json)
        val result        = route(app, fakeRequest).value

        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      s"must return ServiceUnavailable - $SERVICE_UNAVAILABLE response for an invalid json" in {
        val json: JsValue = buildCreateIndvRcaspJson("ZZ@example.com")
        val fakeRequest   = FakeRequest(POST, routes.RcaspController.createRcasp.url).withBody(json)
        val result        = route(app, fakeRequest).value

        status(result) mustBe SERVICE_UNAVAILABLE
      }

      "return 400 BAD_REQUEST for invalid JSON" in {
        val fakeRequest = FakeRequest(POST, routes.RcaspController.createRcasp.url)
          .withJsonBody(Json.obj("invalid" -> "data"))

        val result = route(app, fakeRequest).value

        status(result) mustBe BAD_REQUEST
      }

      "return 400 BAD_REQUEST for missing required fields" in {
        val invalidJson: JsValue = Json.parse("""
              {
                "gbUser": true,
                "idType": "SAFE"
              }
            """)

        val fakeRequest = FakeRequest(POST, routes.RcaspController.createRcasp.url)
          .withJsonBody(invalidJson)

        val result = route(app, fakeRequest).value

        status(result) mustBe BAD_REQUEST
      }
    }
  }

  private def buildCreateOrgRcaspJson(email: String): JsValue =
    Json.parse(
      s"""
         |{
         |  "RCASPManagement": {
         |    "RequestCommon": {
         |      "OriginatingSystem": "MDTP",
         |      "TransmittingSystem": "EIS",
         |      "RequestType": "CREATE",
         |      "Regime": "CARF",
         |      "RequestParameters": [
         |        {
         |          "ParamName": "Test",
         |          "ParamValue": "Test"
         |        }
         |      ]
         |    },
         |    "RequestDetails": {
         |      "RCASPName": "Amazon UK",
         |      "IsRCASPUser": false,
         |      "SubscriptionID": "345567808",
         |      "PartyType": "Organisation",
         |      "TradingName": "Tools for Traders Limited",
         |      "TINDetails": [
         |        {
         |          "TINType": "UTR",
         |          "TIN": "68936493",
         |          "IssuedBy": "GB"
         |        }
         |      ],
         |      "AddressDetails": {
         |        "AddressLine1": "22",
         |        "AddressLine2": "High Street",
         |        "AddressLine3": "Dawley",
         |        "AddressLine4": "Dawley",
         |        "CountryCode": "GB",
         |        "PostalCode": "TF22 2RE"
         |      },
         |      "PrimaryContactDetails": {
         |        "ContactName": "John Smith",
         |        "EmailAddress": "$email",
         |        "PhoneNumber": "0789876568"
         |      },
         |      "SecondaryContactDetails": {
         |        "ContactName": "John Smith",
         |        "EmailAddress": "jdoe@example.com",
         |        "PhoneNumber": "0789876568"
         |      }
         |    }
         |  }
         |}
         |
         |""".stripMargin
    )

  private def buildCreateIndvRcaspJson(email: String): JsValue =
    Json.parse(
      s"""
         |{
         |  "RCASPManagement": {
         |    "RequestCommon": {
         |      "OriginatingSystem": "MDTP",
         |      "TransmittingSystem": "EIS",
         |      "RequestType": "UPDATE",
         |      "Regime": "CARF",
         |      "RequestParameters": [
         |        {
         |          "ParamName": "Test",
         |          "ParamValue": "Test"
         |        }
         |      ]
         |    },
         |    "RequestDetails": {
         |      "RCASPID": "683373339",
         |      "FirstName": "John",
         |      "LastName": "Smith",
         |      "IsRCASPUser": false,
         |      "SubscriptionID": "345567808",
         |      "PartyType": "Individual",
         |      "TINDetails": [
         |        {
         |          "TINType": "OTHER",
         |          "TIN": "68936493",
         |          "IssuedBy": "GB"
         |        }
         |      ],
         |      "AddressDetails": {
         |        "AddressLine1": "22",
         |        "AddressLine2": "High Street",
         |        "AddressLine3": "Dawley",
         |        "AddressLine4": "Dawley",
         |        "CountryCode": "GB",
         |        "PostalCode": "TF22 2RE"
         |      },
         |      "PrimaryContactDetails": {
         |        "ContactName": "John Smith",
         |        "EmailAddress": "$email",
         |        "PhoneNumber": "0789876568"
         |      }
         |    }
         |  }
         |}
         |
         |""".stripMargin
    )
}
