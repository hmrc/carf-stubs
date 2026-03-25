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

package uk.gov.hmrc.carfstubs.controllers

import javax.inject.*
import play.api.mvc.*
import play.api.libs.json.*
import play.api.Logging
import uk.gov.hmrc.carfstubs.models.email.SendEmailRequest

@Singleton
class EmailController @Inject() (cc: ControllerComponents) extends AbstractController(cc) with Logging {

  def send(): Action[JsValue] = Action(parse.json) { request =>
    request.body
      .validate[SendEmailRequest]
      .fold(
        _ => BadRequest(Json.obj("message" -> "Invalid JSON")),
        req => {

          val email = req.to.headOption.map(_.toLowerCase)

          email match {
            case Some("email-stub-success@gmail.com") =>
              logger.info("[STUBS EmailController] Returning 202 (success)")
              Accepted // 202

            case Some("bad-request@gmail.com") =>
              logger.info("[STUBS EmailController] Returning 400 (bad request)")
              BadRequest // 400

            case Some("no-content-header@gmail.com") =>
              logger.info("[STUBS EmailController] Returning 415 (unsupported media type)")
              UnsupportedMediaType(Json.obj("message" -> "Simulated unsupported media type")) // 415

            case Some("email-stub-failure@example.com") =>
              logger.info("[STUBS EmailController] Returning 500 (server error)")
              InternalServerError // 500

            case _ =>
              logger.info("[STUBS EmailController] Returning 202 (default)")
              Accepted
          }
        }
      )
  }
}
