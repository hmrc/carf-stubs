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
import play.api.libs.json.Json
import play.api.test.Helpers.*
import uk.gov.hmrc.carfstubs.controllers.EmailController

class EmailControllerSpec extends SpecBase {

  val controller = new EmailController(cc)

  private def request(email: String) =
    fakeRequestWithJsonBody(
      Json.obj(
        "to"         -> Seq(email),
        "templateId" -> "test-template",
        "parameters" -> Json.obj(
          "name"          -> "John Smith",
          "carfReference" -> "XCARF0012345678"
        ),
        "force"      -> true
      )
    )

  "EmailController.send" - {

    "must return 202 ACCEPTED for success email" in {
      val result = controller.send()(request("email-stub-success@gmail.com"))

      status(result) mustBe ACCEPTED
    }

    "must return 400 BAD_REQUEST for bad request email" in {
      val result = controller.send()(request("bad-request@gmail.com"))

      status(result) mustBe BAD_REQUEST
    }

    "must return 415 UNSUPPORTED_MEDIA_TYPE with message" in {
      val result = controller.send()(request("no-content-header@gmail.com"))

      status(result) mustBe UNSUPPORTED_MEDIA_TYPE

      val json = contentAsJson(result)
      (json \ "message").as[String] mustBe "Simulated unsupported media type"
    }

    "must return 500 INTERNAL_SERVER_ERROR for failure email" in {
      val result = controller.send()(request("email-stub-failure@example.com"))

      status(result) mustBe INTERNAL_SERVER_ERROR
    }

    "must return 202 ACCEPTED for any other email" in {
      val result = controller.send()(request("someone@other.com"))

      status(result) mustBe ACCEPTED
    }

    "must return 400 BAD_REQUEST for invalid JSON" in {
      val result = controller.send()(fakeRequestWithJsonBody(Json.obj("wrong" -> "structure")))

      status(result) mustBe BAD_REQUEST

      val json = contentAsJson(result)
      (json \ "message").as[String] mustBe "Invalid JSON"
    }
  }
}
