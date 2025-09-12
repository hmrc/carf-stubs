/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.carfstubs.controllers

import play.api.mvc.ControllerComponents
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject

class CarfController @Inject() (
    cc: ControllerComponents
) extends BackendController(cc):

  def getDetails: Int = 5
