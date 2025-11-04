/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.carfstubs.controllers

import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.carfstubs.models.request.RegisterWithIDRequest
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import play.api.Logging
import play.api.libs.json.JsValue
import uk.gov.hmrc.carfstubs.helpers.RegistrationHelper

import javax.inject.Inject
import scala.concurrent.Future

class RegistrationController @Inject() (
    cc: ControllerComponents
) extends BackendController(cc)
    with Logging
    with RegistrationHelper:

  def registerIndividualWithId: Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[RegisterWithIDRequest] { request =>
      logger.info(s"%%% LOOK HERE (Stub Request) %%% \n-> $request")
      val response = returnResponse(request)
      logger.info(s"%%% LOOK HERE (Stub Response) %%% \n-> $response")
      Future.successful(response)
    }
  }
