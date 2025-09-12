/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.carfstubs.controllers

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.mvc.ControllerComponents

class CarfControllerSpec extends AnyWordSpec with Matchers {

  val TestCC         = mock[ControllerComponents]
  val TestController = new CarfController(TestCC)

  "carf controller getDetails"     should:
    "return 5" in:
      TestController.getDetails mustEqual 5
}
