package controllers

import base.SpecBase
import play.api.http.Status.FORBIDDEN
import play.api.test.Helpers.status
import uk.gov.hmrc.carfstubs.controllers.RegistrationController

class RegistrationControllerSpec extends SpecBase {

  val testController = new RegistrationController(cc)

  "RegistrationController" - {
    "getDetails method" - {
      "must return 5" in {
        val result = testController.getDetails()(fakeRequest)

        status(result) mustBe FORBIDDEN
      }
    }
  }
}
