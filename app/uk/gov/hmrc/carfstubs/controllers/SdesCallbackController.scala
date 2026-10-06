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

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.pattern
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.carfstubs.config.AppConfig
import uk.gov.hmrc.carfstubs.connectors.SdesCallbackConnector
import uk.gov.hmrc.carfstubs.models.request.CallbackRequest
import uk.gov.hmrc.carfstubs.models.submissionCallback.NotificationType.{FileProcessed, FileProcessingFailure, FileReady}
import uk.gov.hmrc.carfstubs.types.ResultT
import uk.gov.hmrc.carfstubs.utils.LoggerUtil.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SdesCallbackController @Inject() (
    cc: ControllerComponents,
    sdesCallbackConnector: SdesCallbackConnector,
    system: ActorSystem,
    appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def callback: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body
      .validate[CallbackRequest]
      .fold(
        invalid = invalid =>
          logError(
            s"[SdesCallbackController][callback] Failed to parse request body with message: ${invalid.mkString(",\n")}"
          )
          Future.successful(BadRequest("Could not parse request body as CallbackRequest"))
        ,
        valid = sdesCallback =>
          logInfo(
            s"[SdesCallbackController][callback] Received SDES callback. Notification type ${sdesCallback.notification}, file: ${sdesCallback.filename}, conversationId: ${sdesCallback.correlationID}"
          )
          val sdesCallbackRequest = updateStatusOfSdesCallback(sdesCallback)
          sdesCallbackConnector.callback(sdesCallbackRequest).value.flatMap {
            case Left(error) =>
              logWarn(s"[SdesCallbackController][callback] Error from SDES callback: $error")
              Future.successful(InternalServerError("Error from SDES callback"))
            case Right(_)    =>
              sdesCallbackRequest.notification match {
                case FileProcessed =>
                  sendFileReadyCallback(sdesCallbackRequest).value
                    .map(_ => Ok)
                case _             => Future.successful(Ok)
              }
          }
      )
  }

  private def updateStatusOfSdesCallback(sdesCallback: CallbackRequest): CallbackRequest = {
    val containsVirus = sdesCallback.filename.toLowerCase.contains("virus")
    if (
      sdesCallback.notification == FileProcessed &&
      (containsVirus || sdesCallback.filename.toLowerCase.contains("unexpected"))
    ) {
      sdesCallback.copy(
        notification = FileProcessingFailure,
        failureReason = if containsVirus then Some("virus") else None
      )
    } else {
      sdesCallback
    }
  }

  private def sendFileReadyCallback(sdesCallback: CallbackRequest)(implicit hc: HeaderCarrier): ResultT[Unit] = {
    val submittedFileName = sdesCallback.filename.toLowerCase

    val businessRulesFileName: Option[String] = submittedFileName match {
      case name if name.contains("accepted")                          =>
        Some("BusinessRuleCheckSampleRequest_ValidFile_v0.3.xml")
      case name if name.contains("rejected") && name.contains("many") =>
        Some("BusinessRuleCheckSampleRequest_validFile_with_150errors.xml")
      case name if name.contains("rejected")                          =>
        Some("BusinessRuleCheckSampleRequest_validFile_with_errors.xml")
      case name if name.contains("schema-error")                      =>
        Some("BusinessRuleCheckSampleRequest_invalidFile_schema__errors.xml")
      case name if name.contains("malformed")                         =>
        Some("malformed-xml.xml")
      case name if name.contains("not-found")                         =>
        Some("unknown.xml")
      case name if name.contains("unlisted")                          =>
        Some("unlisted.xml")
      case _                                                          =>
        None
    }

    businessRulesFileName.fold(
      ResultT.fromValue(())
    ) { filename =>
      val fileReadyCallback = sdesCallback.copy(notification = FileReady, filename = filename)

      val delayTime: FiniteDuration =
        if (submittedFileName.contains("slow")) { appConfig.slowCallbackTimeInSeconds.seconds }
        else { appConfig.fastCallbackTimeInSeconds.seconds }

      ResultT.fromFuture {
        pattern.after(delayTime, system.scheduler) {
          sdesCallbackConnector.callback(fileReadyCallback).value
        }
      }
    }
  }

}
