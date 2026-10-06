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
import org.mockito.Mockito.when
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.carfstubs.config.AppConfig
import uk.gov.hmrc.carfstubs.controllers.routes
import uk.gov.hmrc.carfstubs.models.submissionCallback.BusinessRulesFileListing

class FileListingControllerSpec extends SpecBase {

  val mockAppConfig: AppConfig = mock[AppConfig]

  private def application(): Application =
    new GuiceApplicationBuilder()
      .overrides(bind[AppConfig].toInstance(mockAppConfig))
      .build()

  private val informationType: String = "carf-submission"
  private val clientId: String        = "client-id"
  private val srn: String             = "123456789"

  when(mockAppConfig.informationType).thenReturn(informationType)
  when(mockAppConfig.clientId).thenReturn(clientId)
  when(mockAppConfig.srn).thenReturn(srn)

  "FileListingController" - {
    ".getBusinessRulesFileListing" - {
      "must return 200 with the list of business rules files saved in the backend" in {
        val request =
          FakeRequest(GET, routes.FileListingController.getBusinessRulesFileListing(informationType).url)
            .withHeaders("x-client-id" -> clientId, "x-sdes-key" -> srn)
        val result  = route(application(), request).value

        status(result)        mustBe OK
        contentAsJson(result) mustBe Json.toJson(BusinessRulesFileListing.businessRulesFilesInBackend)
      }

      "must return 400 when informationType is not the expected value" in {
        val request =
          FakeRequest(GET, routes.FileListingController.getBusinessRulesFileListing("wrong-info-type").url)
            .withHeaders("x-client-id" -> clientId, "x-sdes-key" -> srn)
        val result  = route(application(), request).value

        status(result)        mustBe BAD_REQUEST
        contentAsString(result) must include("Request does not contain required informationType or headers")
      }

      "must return 400 when x-client-id is missing from headers" in {
        val request =
          FakeRequest(GET, routes.FileListingController.getBusinessRulesFileListing(informationType).url)
            .withHeaders("x-sdes-key" -> srn)
        val result  = route(application(), request).value

        status(result)        mustBe BAD_REQUEST
        contentAsString(result) must include("Request does not contain required informationType or headers")
      }

      "must return 400 when x-sdes-key is missing from headers" in {
        val request =
          FakeRequest(GET, routes.FileListingController.getBusinessRulesFileListing(informationType).url)
            .withHeaders("x-client-id" -> clientId)
        val result  = route(application(), request).value

        status(result)        mustBe BAD_REQUEST
        contentAsString(result) must include("Request does not contain required informationType or headers")
      }

      "must return 400 when x-client-id is not the expected value" in {
        val request =
          FakeRequest(GET, routes.FileListingController.getBusinessRulesFileListing(informationType).url)
            .withHeaders("x-client-id" -> "wrong-client-id", "x-sdes-key" -> srn)
        val result  = route(application(), request).value

        status(result)        mustBe BAD_REQUEST
        contentAsString(result) must include("Request does not contain required informationType or headers")
      }

      "must return 400 when x-sdes-key is not the expected value" in {
        val request =
          FakeRequest(GET, routes.FileListingController.getBusinessRulesFileListing(informationType).url)
            .withHeaders("x-client-id" -> clientId, "x-sdes-key" -> "987654321")
        val result  = route(application(), request).value

        status(result)        mustBe BAD_REQUEST
        contentAsString(result) must include("Request does not contain required informationType or headers")
      }
    }
  }
}
