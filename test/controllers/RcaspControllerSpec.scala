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
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.carfstubs.controllers.routes
import uk.gov.hmrc.carfstubs.models.response.{IndividualRcaspDetails, OrganisationRcaspDetails, RcaspDetails}

class RcaspControllerSpec extends SpecBase with OptionValues {

  "RcaspController" - {
    "viewRcasp" - {

      s"must return Ok - $OK response with full individual response for a valid CARFID" in {
        val request = FakeRequest(GET, routes.RcaspController.viewRcasp("CCCAR0024000102", "683373339").url)
        val result  = route(app, request).value

        status(result) mustBe OK
        val rcaspDetails = (contentAsJson(result) \ "ViewRCASP" \ "ResponseDetails" \ "RCASPList")
          .as[List[IndividualRcaspDetails]]
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
          .as[List[OrganisationRcaspDetails]]
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
          .as[List[IndividualRcaspDetails]]
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
          .as[List[OrganisationRcaspDetails]]
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
          .as[List[IndividualRcaspDetails]]
          .length      mustBe 2
      }

      s"must return Ok - $OK response with multiple RCASP items in organisation response for a valid CARFID starting with NN" in {
        val request = FakeRequest(GET, routes.RcaspController.viewRcasp("NNCAR0024000102", "683373339").url)
        val result  = route(app, request).value

        status(result) mustBe OK
        (contentAsJson(result) \ "ViewRCASP" \ "ResponseDetails" \ "RCASPList")
          .as[List[OrganisationRcaspDetails]]
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
  }
}
