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

import play.api.Logging
import play.api.libs.json.*
import play.api.mvc.*
import uk.gov.hmrc.carfstubs.helpers.RcaspHelper
import uk.gov.hmrc.carfstubs.models.request.createRcasp.RcaspRequest as CreateRcaspRequest
import uk.gov.hmrc.carfstubs.models.request.deleteRcasp.RcaspRequest as DeleteRcaspRequest
import uk.gov.hmrc.carfstubs.models.request.updateRcasp.RcaspRequest as UpdateRcaspRequest
import uk.gov.hmrc.carfstubs.models.{Create, Delete, RequestType, Update}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.Future

class RcaspController @Inject() (cc: ControllerComponents) extends BackendController(cc) with Logging with RcaspHelper:

  def viewRcasp(carfId: String, rcaspId: String): Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"View RCASP Request received with carfID: $carfId, rcaspId: $rcaspId")
    val response: Result = returnRcaspResponse(carfId)
    logger.info(
      s"Response Code \n-> ${response.header.status}" +
        s"\nResponse Body \n-> $response"
    )
    Future.successful(response)
  }

  def createUpdateOrDeleteRcasp: Action[JsValue] =
    Action.async(parse.json) { implicit request =>
      val requestTypeOpt = (request.body \\ "RequestType").headOption.flatMap(_.asOpt[String]).map(_.toUpperCase)

      requestTypeOpt match {
        case Some("CREATE") => createRcasp
        case Some("UPDATE") => updateRcasp
        case Some("DELETE") => deleteRcasp
        case Some(unknown)  =>
          logger.warn(s"Unsupported RequestType received: $unknown")
          Future.successful(BadRequest(s"Unsupported RequestType: $unknown"))
        case None           =>
          logger.warn("RequestType missing from JSON payload")
          Future.successful(BadRequest("RequestType missing from JSON payload"))
      }
    }

  private def updateRcasp(implicit request: Request[JsValue]): Future[Result] = {
    val jsResult = request.body.validate[UpdateRcaspRequest]
    processRcasp(Update("RCASP"), jsResult) { payload =>
      returnUpdateResponse(payload)
    }
  }

  private def createRcasp(implicit request: Request[JsValue]): Future[Result] = {
    val jsResult = request.body.validate[CreateRcaspRequest]
    processRcasp(Create("RCASP"), jsResult) { payload =>
      returnCreateResponse(payload)
    }
  }

  private def deleteRcasp(implicit request: Request[JsValue]): Future[Result] = {
    val jsResult = request.body.validate[DeleteRcaspRequest]
    processRcasp(Delete("RCASP"), jsResult) { payload =>
      returnDeleteResponse(payload)
    }
  }

  private def processRcasp[A](
      requestType: RequestType,
      jsResult: JsResult[A]
  )(f: A => Result)(implicit request: Request[JsValue]): Future[Result] = {

    logger.info(s"Management RCASP ${requestType.name} Request received: \n -> ${Json.prettyPrint(request.body)}")

    jsResult match {
      case JsSuccess(payload, _) =>
        logger.debug("Json validation success")
        val response: Result = f(payload)
        logger.info(
          s"${requestType.printFunctionName} Stub returned Response Code \n-> ${response.header.status}"
        )
        Future.successful(response)

      case JsError(errors) =>
        logger.error(s"Invalid ${requestType.printFunctionName} request payload: ${errors.mkString(", ")}")
        Future.successful(
          BadRequest(s"Invalid ${requestType.printFunctionName} payload: ${errors.mkString(", ")}")
            .withHeaders("ERROR_TYPE" -> "JSON_ERROR")
        )
    }
  }
