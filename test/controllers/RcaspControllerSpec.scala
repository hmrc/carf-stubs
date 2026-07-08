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
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.carfstubs.controllers.routes
import uk.gov.hmrc.carfstubs.models.*
import uk.gov.hmrc.carfstubs.models.request.createRcasp.RcaspRequest
import uk.gov.hmrc.carfstubs.models.request.{createRcasp, deleteRcasp, updateRcasp, RcaspRequestCommon, RequestParameter}
import uk.gov.hmrc.carfstubs.models.response.{SubmitRcaspResponse, SubmitResponseDetails, SubmitReturnParameters}

class RcaspControllerSpec extends SpecBase {

  "RcaspController" - {
    "viewRcasp" - {
      s"must return Ok - $OK response with full individual response for a valid CARFID" in {
        val request = FakeRequest(GET, routes.RcaspController.viewRcasp("XCCAR0024000102", "none").url)
        val result  = route(app, request).value

        status(result) mustBe OK
        val rcaspDetails = (contentAsJson(result) \ "ViewRCASP" \ "ResponseDetails" \ "RCASPList")
          .as[List[viewAndUpdateRcasp.IndividualRcaspDetails]]
          .head
        rcaspDetails.SubscriptionID        mustBe "XCCAR0024000102"
        rcaspDetails.RCASPID               mustBe "ZMCAR0123456780"
        rcaspDetails.PartyType             mustBe "Individual"
        rcaspDetails.PrimaryContactDetails mustBe defined
      }

      s"must return Ok - $OK response with full organisation response for a valid CARFID with second letter R" in {
        val request = FakeRequest(GET, routes.RcaspController.viewRcasp("XRCAR0024000102", "none").url)
        val result  = route(app, request).value

        status(result) mustBe OK
        val rcaspDetails = (contentAsJson(result) \ "ViewRCASP" \ "ResponseDetails" \ "RCASPList")
          .as[List[viewAndUpdateRcasp.OrganisationRcaspDetails]]
          .head
        rcaspDetails.SubscriptionID          mustBe "XRCAR0024000102"
        rcaspDetails.RCASPID                 mustBe "ZMCAR0123456785"
        rcaspDetails.PartyType               mustBe "Organisation"
        rcaspDetails.PrimaryContactDetails   mustBe defined
        rcaspDetails.SecondaryContactDetails mustBe defined
      }

      s"must return Ok - $OK response with empty optional fields in individual response for a valid CARFID with second letter M" in {
        val request = FakeRequest(GET, routes.RcaspController.viewRcasp("XMCAR0024000102", "none").url)
        val result  = route(app, request).value

        status(result) mustBe OK
        val rcaspDetails = (contentAsJson(result) \ "ViewRCASP" \ "ResponseDetails" \ "RCASPList")
          .as[List[viewAndUpdateRcasp.IndividualRcaspDetails]]
          .head

        rcaspDetails.SubscriptionID mustBe "XMCAR0024000102"
        rcaspDetails.RCASPID        mustBe "ZMCAR0123456781"
        rcaspDetails.PartyType      mustBe "Individual"

        rcaspDetails.PrimaryContactDetails                        mustBe defined
        rcaspDetails.PrimaryContactDetails.flatMap(_.PhoneNumber) mustBe empty
      }

      s"must return Ok - $OK response with empty optional fields in organisation response for a valid CARFID with second letter O" in {
        val request = FakeRequest(GET, routes.RcaspController.viewRcasp("XOCAR0024000102", "none").url)
        val result  = route(app, request).value

        status(result) mustBe OK
        val rcaspDetails = (contentAsJson(result) \ "ViewRCASP" \ "ResponseDetails" \ "RCASPList")
          .as[List[viewAndUpdateRcasp.OrganisationRcaspDetails]]
          .head

        rcaspDetails.SubscriptionID mustBe "XOCAR0024000102"
        rcaspDetails.RCASPID        mustBe "ZMCAR0123456786"
        rcaspDetails.PartyType      mustBe "Organisation"

        rcaspDetails.PrimaryContactDetails                        mustBe defined
        rcaspDetails.PrimaryContactDetails.flatMap(_.PhoneNumber) mustBe empty
        rcaspDetails.SecondaryContactDetails                      mustBe empty
      }

      s"must return Ok - $OK response with multiple RCASP items in individual response for a valid CARFID with second letter L" in {
        val request = FakeRequest(GET, routes.RcaspController.viewRcasp("XLCAR0024000102", "none").url)
        val result  = route(app, request).value

        status(result) mustBe OK
        (contentAsJson(result) \ "ViewRCASP" \ "ResponseDetails" \ "RCASPList")
          .as[List[viewAndUpdateRcasp.IndividualRcaspDetails]]
          .length      mustBe 5
      }

      s"must return Ok - $OK response with multiple RCASP items in organisation response for a valid CARFID with second letter N" in {
        val request = FakeRequest(GET, routes.RcaspController.viewRcasp("XNCAR0024000102", "none").url)
        val result  = route(app, request).value

        status(result) mustBe OK
        (contentAsJson(result) \ "ViewRCASP" \ "ResponseDetails" \ "RCASPList")
          .as[List[viewAndUpdateRcasp.OrganisationRcaspDetails]]
          .length      mustBe 5
      }

      s"must return Ok - $OK response with no RCASP items in response for a valid CARFID with second letter K" in {
        val request = FakeRequest(GET, routes.RcaspController.viewRcasp("XKCAR0024000102", "none").url)
        val result  = route(app, request).value

        status(result) mustBe OK
        (contentAsJson(result) \ "ViewRCASP" \ "ResponseDetails" \ "RCASPList")
          .as[List[viewAndUpdateRcasp.RcaspDetails]]
          .length      mustBe 0
      }

      s"must return Internal Server Error - $INTERNAL_SERVER_ERROR response for a valid CARFID with second letter Y" in {
        val request = FakeRequest(GET, routes.RcaspController.viewRcasp("XYCAR0024000102", "none").url)
        val result  = route(app, request).value

        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      s"must return Bad Request - $BAD_REQUEST response for a valid CARFID with second letter T" in {
        val request = FakeRequest(GET, routes.RcaspController.viewRcasp("XTCAR0024000102", "none").url)
        val result  = route(app, request).value

        status(result) mustBe BAD_REQUEST
      }

      s"must return Unprocessable Entity - $UNPROCESSABLE_ENTITY response for a valid CARFID with second letter P" in {
        val request = FakeRequest(GET, routes.RcaspController.viewRcasp("XPCAR0024000102", "none").url)
        val result  = route(app, request).value

        status(result) mustBe UNPROCESSABLE_ENTITY
      }

      s"must return Service Unavailable - $SERVICE_UNAVAILABLE response for a valid CARFID with second letter S" in {
        val request = FakeRequest(GET, routes.RcaspController.viewRcasp("XSCAR0024000102", "none").url)
        val result  = route(app, request).value

        status(result) mustBe SERVICE_UNAVAILABLE
      }
    }

    "createUpdateOrDeleteRcasp" - {
      val submitRcaspResponse = SubmitRcaspResponse(
        SubmitResponseDetails(
          SubmitReturnParameters("RCASPID", "ZMCAR0123456789")
        )
      )

      Seq("0", "1", "2", "3").foreach { digit =>
        s"must return Ok - $OK response for a valid json with organisation in request (registered business) when the carfId ends with $digit" in {
          val json: JsValue = buildCreateRegisteredBusinessRcaspJson(s"XCARF00000000$digit")
          val request       = FakeRequest(POST, routes.RcaspController.createUpdateOrDeleteRcasp.url).withBody(json)
          val result        = route(app, request).value

          status(result)        mustBe OK
          contentAsJson(result) mustBe Json.toJson(submitRcaspResponse)
        }

        s"must return Ok - $OK response for a valid json with organisation in request (not registered business) when the carfId ends with $digit" in {
          val json: JsValue = buildCreateOrgRcaspJson(s"XCARF00000000$digit")
          val request       = FakeRequest(POST, routes.RcaspController.createUpdateOrDeleteRcasp.url).withBody(json)
          val result        = route(app, request).value

          status(result)        mustBe OK
          contentAsJson(result) mustBe Json.toJson(submitRcaspResponse)
        }

        s"must return Ok - $OK response for a valid json with individual in request when the carfId ends with $digit" in {
          val json: JsValue = buildCreateIndvRcaspJson(s"XCARF00000000$digit")
          val fakeRequest   = FakeRequest(POST, routes.RcaspController.createUpdateOrDeleteRcasp.url).withBody(json)
          val result        = route(app, fakeRequest).value

          status(result)        mustBe OK
          contentAsJson(result) mustBe Json.toJson(submitRcaspResponse)
        }

        s"must return Ok - $OK response for a valid update json with individual in request with $digit" in {
          val json: JsValue = Json.parse(
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
               |      "RCASPID": "ZMCAR0123456780",
               |      "SubscriptionID": "XCARF00000000$digit",
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
               |        "EmailAddress": "jdoe@example.com",
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
               |""".stripMargin
          )
          val fakeRequest   = FakeRequest(POST, routes.RcaspController.createUpdateOrDeleteRcasp.url).withBody(json)
          val result        = route(app, fakeRequest).value

          status(result) mustBe OK
        }

        s"must return Ok - $OK response for a valid delete json with individual in request with $digit" in {
          val json: JsValue = Json.parse(
            s"""
               |{
               |  "RCASPManagement": {
               |    "RequestCommon": {
               |      "OriginatingSystem": "MDTP",
               |      "TransmittingSystem": "EIS",
               |      "RequestType": "DELETE",
               |      "Regime": "CARF",
               |      "RequestParameters": [
               |        {
               |          "ParamName": "TEST",
               |          "ParamValue": "TEST"
               |        }
               |      ]
               |    },
               |    "RequestDetails": {
               |      "RCASPID": "683373339",
               |      "SubscriptionID": "XCARF00000000$digit"
               |    }
               |  }
               |}
               |""".stripMargin
          )
          val fakeRequest   = FakeRequest(POST, routes.RcaspController.createUpdateOrDeleteRcasp.url).withBody(json)
          val result        = route(app, fakeRequest).value

          status(result) mustBe OK
        }

        s"must return Ok - $OK response for a valid json from model when the carfId ends with $digit" in {
          val json: JsValue = Json.toJson(
            createRcasp.RcaspRequest(
              createRcasp.RcaspManagementRequest(
                RcaspRequestCommon(
                  OriginatingSystem = "MDTP",
                  TransmittingSystem = "EIS",
                  RequestType = "CREATE",
                  Regime = "CARF",
                  RequestParameters = None
                ),
                createRcasp.IndividualRcaspDetails(
                  SubscriptionID = s"XCARF00000000$digit",
                  IsRCASPUser = true,
                  PartyType = "Individual",
                  FirstName = "Penny",
                  LastName = "Cassiopeia",
                  TINDetails = Some(
                    List(
                      TinDetails(
                        TINType = "OTHER",
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
        }

        s"must return Ok - $OK response for a valid create json from model with $digit" in {
          val json: JsValue = Json.toJson(
            createRcasp.RcaspRequest(
              createRcasp.RcaspManagementRequest(
                RcaspRequestCommon(
                  OriginatingSystem = "CADX",
                  TransmittingSystem = "EIS",
                  RequestType = "CREATE",
                  Regime = "CARF",
                  RequestParameters = Option(List(RequestParameter("key", "value")))
                ),
                createRcasp.IndividualRcaspDetails(
                  SubscriptionID = s"XCARF00000000$digit",
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

          val fakeRequest = FakeRequest(POST, routes.RcaspController.createUpdateOrDeleteRcasp.url).withBody(json)
          val result      = route(app, fakeRequest).value

          status(result) mustBe OK
        }

        s"must return Ok - $OK response for a valid update json from model with $digit" in {
          val json: JsValue = Json.toJson(
            updateRcasp.RcaspRequest(
              updateRcasp.RcaspManagementRequest(
                RcaspRequestCommon(
                  OriginatingSystem = "CADX",
                  TransmittingSystem = "EIS",
                  RequestType = "CREATE",
                  Regime = "CARF",
                  RequestParameters = Option(List(RequestParameter("key", "value")))
                ),
                viewAndUpdateRcasp.IndividualRcaspDetails(
                  RCASPID = "ZMCAR0123456780",
                  SubscriptionID = s"XCARF00000000$digit",
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

          val fakeRequest = FakeRequest(POST, routes.RcaspController.createUpdateOrDeleteRcasp.url).withBody(json)
          val result      = route(app, fakeRequest).value

          status(result)        mustBe OK
          contentAsJson(result) mustBe Json.toJson(submitRcaspResponse)
        }

        s"must return Ok - $OK response for a valid delete json from model with $digit" in {
          val json: JsValue = Json.toJson(
            deleteRcasp.RcaspRequest(
              deleteRcasp.RcaspManagementRequest(
                RcaspRequestCommon(
                  OriginatingSystem = "CADX",
                  TransmittingSystem = "EIS",
                  RequestType = "DELETE",
                  Regime = "CARF",
                  RequestParameters = Option(List(RequestParameter("key", "value")))
                ),
                deleteRcasp.RcaspDetails(
                  RCASPID = "ZMCAR0123456780",
                  SubscriptionID = s"XCARF00000000$digit"
                )
              )
            )
          )

          val fakeRequest = FakeRequest(POST, routes.RcaspController.createUpdateOrDeleteRcasp.url).withBody(json)
          val result      = route(app, fakeRequest).value

          status(result)        mustBe OK
          contentAsJson(result) mustBe Json.toJson(submitRcaspResponse)
        }
      }

      s"must return UnprocessableEntity - $UNPROCESSABLE_ENTITY response when the carfId ends with 9" in {
        val json: JsValue = buildCreateIndvRcaspJson("XCARF000000009")
        val fakeRequest   = FakeRequest(POST, routes.RcaspController.createUpdateOrDeleteRcasp.url).withBody(json)
        val result        = route(app, fakeRequest).value

        status(result) mustBe UNPROCESSABLE_ENTITY
      }

      s"must return Forbidden - $FORBIDDEN response when the carfId ends with 8" in {
        val json: JsValue = buildCreateIndvRcaspJson("XCARF000000008")
        val fakeRequest   = FakeRequest(POST, routes.RcaspController.createUpdateOrDeleteRcasp.url).withBody(json)
        val result        = route(app, fakeRequest).value

        status(result) mustBe FORBIDDEN
      }

      s"must return MethodNotAllowed - $METHOD_NOT_ALLOWED response when the carfId ends with 7" in {
        val json: JsValue = buildCreateIndvRcaspJson("XCARF000000007")
        val fakeRequest   = FakeRequest(POST, routes.RcaspController.createUpdateOrDeleteRcasp.url).withBody(json)
        val result        = route(app, fakeRequest).value

        status(result) mustBe METHOD_NOT_ALLOWED
      }

      s"must return BadRequest - $BAD_REQUEST response when the carfId ends with 6" in {
        val json: JsValue = buildCreateIndvRcaspJson("XCARF000000006")
        val fakeRequest   = FakeRequest(POST, routes.RcaspController.createUpdateOrDeleteRcasp.url).withBody(json)
        val result        = route(app, fakeRequest).value

        status(result) mustBe BAD_REQUEST
      }

      s"must return InternalServerError - $INTERNAL_SERVER_ERROR response when the carfId ends with 5" in {
        val json: JsValue = buildCreateIndvRcaspJson("XCARF000000005")
        val fakeRequest   = FakeRequest(POST, routes.RcaspController.createUpdateOrDeleteRcasp.url).withBody(json)
        val result        = route(app, fakeRequest).value

        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      s"must return ServiceUnavailable - $SERVICE_UNAVAILABLE response when the carfId ends with 4" in {
        val json: JsValue = buildCreateIndvRcaspJson("XCARF000000004")
        val fakeRequest   = FakeRequest(POST, routes.RcaspController.createUpdateOrDeleteRcasp.url).withBody(json)
        val result        = route(app, fakeRequest).value

        status(result) mustBe SERVICE_UNAVAILABLE
      }

      "return 400 BAD_REQUEST for invalid JSON" in {
        val fakeRequest = FakeRequest(POST, routes.RcaspController.createUpdateOrDeleteRcasp.url)
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

        val fakeRequest = FakeRequest(POST, routes.RcaspController.createUpdateOrDeleteRcasp.url)
          .withJsonBody(invalidJson)

        val result = route(app, fakeRequest).value

        status(result) mustBe BAD_REQUEST
      }
    }
  }

  private def buildCreateRegisteredBusinessRcaspJson(carfId: String): JsValue =
    Json.parse(
      s"""
         |{
         |  "RCASPManagement": {
         |    "RequestCommon": {
         |      "OriginatingSystem": "MDTP",
         |      "TransmittingSystem": "EIS",
         |      "RequestType": "CREATE",
         |      "Regime": "CARF"
         |    },
         |    "RequestDetails": {
         |      "RCASPName": "Amazon UK",
         |      "IsRCASPUser": true,
         |      "SubscriptionID": "$carfId",
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
         |      }
         |    }
         |  }
         |}
         |""".stripMargin
    )

  private def buildCreateOrgRcaspJson(carfId: String): JsValue =
    Json.parse(
      s"""
         |{
         |  "RCASPManagement": {
         |    "RequestCommon": {
         |      "OriginatingSystem": "MDTP",
         |      "TransmittingSystem": "EIS",
         |      "RequestType": "CREATE",
         |      "Regime": "CARF"
         |    },
         |    "RequestDetails": {
         |      "RCASPName": "Amazon UK",
         |      "IsRCASPUser": false,
         |      "SubscriptionID": "$carfId",
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
         |        "EmailAddress": "john.smith@example.com",
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

  private def buildCreateIndvRcaspJson(carfId: String): JsValue =
    Json.parse(
      s"""
         |{
         |  "RCASPManagement": {
         |    "RequestCommon": {
         |      "OriginatingSystem": "MDTP",
         |      "TransmittingSystem": "EIS",
         |      "RequestType": "CREATE",
         |      "Regime": "CARF"
         |    },
         |    "RequestDetails": {
         |      "FirstName": "John",
         |      "LastName": "Smith",
         |      "IsRCASPUser": false,
         |      "SubscriptionID": "$carfId",
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
         |        "EmailAddress": "john.smith@example.com",
         |        "PhoneNumber": "0789876568"
         |      }
         |    }
         |  }
         |}
         |
         |""".stripMargin
    )

}
