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

package uk.gov.hmrc.carfstubs.helpers

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.mvc.Results.*
import uk.gov.hmrc.carfstubs.models.*
import uk.gov.hmrc.carfstubs.models.request.{createRcasp, deleteRcasp, updateRcasp}
import uk.gov.hmrc.carfstubs.models.response.*
import uk.gov.hmrc.carfstubs.models.viewAndUpdateRcasp.{OrganisationRcaspDetails, RcaspDetails}
import uk.gov.hmrc.carfstubs.utils.HelperUtil.errorDetailJson

trait RcaspHelper extends Logging {

  def returnRcaspResponse(carfId: String): Result =
    carfId.slice(1, 2).toUpperCase match {
      case "Y" => internalServerError500Response
      case "T" => badRequest400Response
      case "S" => serviceUnavailable503Response
      case "R" => Ok(Json.toJson(fullOrganisationRcaspResponse(carfId)))
      case "P" => unprocessableEntity422Response
      case "O" => Ok(Json.toJson(emptyOptionalsOrganisationRcaspResponse(carfId)))
      case "N" => Ok(Json.toJson(multipleOrganisationRcaspResponse(carfId)))
      case "M" => Ok(Json.toJson(emptyOptionalsIndividualRcaspResponse(carfId)))
      case "L" => Ok(Json.toJson(multipleIndividualRcaspResponse(carfId)))
      case "K" => noRcasps422Response
      case "J" => Ok(Json.toJson(fullIndividualRcaspResponse(carfId)))
      case _   => Ok(Json.toJson(allScenarioRcaspResponse(carfId)))
    }

  def returnCreateResponse(request: createRcasp.RcaspRequest): Result = {
    logger.info("Received Create RCASP management request")
    generateResponse(request.RCASPManagement.RequestDetails.SubscriptionID)
  }

  def returnUpdateResponse(request: updateRcasp.RcaspRequest): Result = {
    logger.info("Received Update RCASP management request")
    generateResponse(request.RCASPManagement.RequestDetails.SubscriptionID)
  }

  def returnDeleteResponse(request: deleteRcasp.RcaspRequest): Result = {
    logger.info("Received Delete RCASP management request")
    generateResponse(request.RCASPManagement.RequestDetails.SubscriptionID)
  }

  private def generateResponse(subscriptionID: String) =
    subscriptionID.takeRight(1) match {
      case "9" => unprocessableEntity422Response
      case "8" => forbiddenResponse
      case "7" => notAllowedResponse
      case "6" => badRequest400Response
      case "5" => internalServerError500Response
      case "4" => serviceUnavailable503Response
      case _   => successfulCreateResponse
    }

  private def successfulCreateResponse: Result =
    Ok(
      Json.toJson(
        SubmitRcaspResponse(
          SubmitResponseDetails(
            SubmitReturnParameters("RCASPID", "ZMCAR0123456789")
          )
        )
      )
    )

  private def fullIndividualRcaspResponse(carfId: String) = ViewRcaspResponse(
    ViewRCASP = ViewRcasp(
      ResponseCommon = rcaspResponseCommon,
      ResponseDetails = RcaspResponseDetails(RCASPList =
        List(
          fullIndividualRcaspDetails(
            carfId,
            rcaspId = "ZMCAR0123456780",
            isRCaspUser = false,
            firstName = "Penny",
            lastName = "Cassiopeia"
          )
        )
      )
    )
  )

  private def allScenarioRcaspResponse(carfId: String) = ViewRcaspResponse(
    ViewRCASP = ViewRcasp(
      ResponseCommon = rcaspResponseCommon,
      ResponseDetails = RcaspResponseDetails(RCASPList =
        List(
          fullIndividualRcaspDetails(
            carfId,
            rcaspId = "ZMCAR0123456780",
            isRCaspUser = false,
            firstName = "Penny",
            lastName = "Cassiopeia"
          ),
          emptyOptionalsIndividualRcaspDetails(
            carfId,
            rcaspId = "ZMCAR0123456782",
            isRCaspUser = false,
            firstName = "Nemona",
            lastName = "Champion"
          ),
          fullOrganisationRcaspDetails(carfId, rcaspId = "ZMCAR0123456786", rcaspName = "Apple"),
          registeredBusinessRcaspDetails(carfId, rcaspId = "ZMCAR0123456787", rcaspName = "Timmy's Turtles"),
          emptyOptionalsOrganisationRcaspDetails(carfId, rcaspId = "ZMCAR0123456788", rcaspName = "Amazon UK")
        )
      )
    )
  )

  private def emptyOptionalsIndividualRcaspResponse(carfId: String) = ViewRcaspResponse(
    ViewRCASP = ViewRcasp(
      ResponseCommon = rcaspResponseCommon,
      ResponseDetails = RcaspResponseDetails(RCASPList =
        List(
          emptyOptionalsIndividualRcaspDetails(
            carfId,
            rcaspId = "ZMCAR0123456781",
            isRCaspUser = false,
            firstName = "Nemona",
            lastName = "Champion"
          )
        )
      )
    )
  )

  private def multipleIndividualRcaspResponse(carfId: String) = ViewRcaspResponse(
    ViewRCASP = ViewRcasp(
      ResponseCommon = rcaspResponseCommon,
      ResponseDetails = RcaspResponseDetails(RCASPList =
        List(
          fullIndividualRcaspDetails(
            carfId,
            rcaspId = "ZMCAR0123456780",
            isRCaspUser = false,
            firstName = "Penny",
            lastName = "Smith"
          ),
          emptyOptionalsIndividualRcaspDetails(
            carfId,
            rcaspId = "ZMCAR0123456781",
            isRCaspUser = false,
            firstName = "Penny",
            lastName = "Cassiopeia"
          ),
          emptyOptionalsIndividualRcaspDetails(
            carfId,
            rcaspId = "ZMCAR0123456782",
            isRCaspUser = true,
            firstName = "Nemona",
            lastName = "Champion"
          ),
          fullIndividualRcaspDetails(
            carfId,
            rcaspId = "ZMCAR0123456783",
            isRCaspUser = false,
            firstName = "Bob",
            lastName = "Smith"
          ),
          fullIndividualRcaspDetails(
            carfId,
            rcaspId = "ZMCAR0123456784",
            isRCaspUser = false,
            firstName = "John",
            lastName = "Doe"
          )
        )
      )
    )
  )

  private def fullIndividualRcaspDetails(
      carfId: String,
      rcaspId: String,
      isRCaspUser: Boolean,
      firstName: String,
      lastName: String
  ): viewAndUpdateRcasp.RcaspDetails =
    viewAndUpdateRcasp.IndividualRcaspDetails(
      RCASPID = rcaspId,
      SubscriptionID = carfId,
      IsRCASPUser = isRCaspUser,
      PartyType = "Individual",
      FirstName = firstName,
      LastName = lastName,
      TINDetails = Some(
        List(
          TinDetails(
            TINType = "OTHER",
            TIN = "AA123456C",
            IssuedBy = "GB"
          )
        )
      ),
      AddressDetails = fullAddress,
      PrimaryContactDetails = Some(
        RcaspContactDetails(
          ContactName = s"$firstName $lastName",
          EmailAddress = "penny.cassiopeia@uva.edu.org",
          PhoneNumber = Some("07123412345")
        )
      )
    )

  private def emptyOptionalsIndividualRcaspDetails(
      carfId: String,
      rcaspId: String,
      isRCaspUser: Boolean,
      firstName: String,
      lastName: String
  ) =
    viewAndUpdateRcasp.IndividualRcaspDetails(
      RCASPID = rcaspId,
      SubscriptionID = carfId,
      IsRCASPUser = isRCaspUser,
      PartyType = "Individual",
      FirstName = firstName,
      LastName = lastName,
      TINDetails = Some(
        List(
          TinDetails(
            TINType = "OTHER",
            TIN = "AA123456C",
            IssuedBy = "GB"
          )
        )
      ),
      AddressDetails = fullAddress,
      PrimaryContactDetails = Some(
        RcaspContactDetails(
          ContactName = s"$firstName $lastName",
          EmailAddress = "penny.cassiopeia@uva.edu.org",
          PhoneNumber = None
        )
      )
    )

  private def fullOrganisationRcaspResponse(carfId: String) = ViewRcaspResponse(
    ViewRCASP = ViewRcasp(
      ResponseCommon = rcaspResponseCommon,
      ResponseDetails = RcaspResponseDetails(RCASPList =
        List(
          fullOrganisationRcaspDetails(carfId, rcaspId = "ZMCAR0123456785", rcaspName = "Mesagoza")
        )
      )
    )
  )

  private def emptyOptionalsOrganisationRcaspResponse(carfId: String) = ViewRcaspResponse(
    ViewRCASP = ViewRcasp(
      ResponseCommon = rcaspResponseCommon,
      ResponseDetails = RcaspResponseDetails(RCASPList =
        List(
          emptyOptionalsOrganisationRcaspDetails(carfId, rcaspId = "ZMCAR0123456786", rcaspName = "Amazon UK")
        )
      )
    )
  )

  private def multipleOrganisationRcaspResponse(carfId: String) = ViewRcaspResponse(
    ViewRCASP = ViewRcasp(
      ResponseCommon = rcaspResponseCommon,
      ResponseDetails = RcaspResponseDetails(RCASPList =
        List(
          fullOrganisationRcaspDetails(carfId, rcaspId = "ZMCAR0123456785", rcaspName = "Mesagoza"),
          fullOrganisationRcaspDetails(carfId, rcaspId = "ZMCAR0123456786", rcaspName = "Apple"),
          registeredBusinessRcaspDetails(carfId, rcaspId = "ZMCAR0123456787", rcaspName = "Timmy's Turtles"),
          emptyOptionalsOrganisationRcaspDetails(carfId, rcaspId = "ZMCAR0123456788", rcaspName = "Amazon UK"),
          emptyOptionalsOrganisationRcaspDetails(carfId, rcaspId = "ZMCAR0123456789", rcaspName = "Name Name")
        )
      )
    )
  )

  private def registeredBusinessRcaspDetails(carfId: String, rcaspId: String, rcaspName: String) =
    viewAndUpdateRcasp.OrganisationRcaspDetails(
      SubscriptionID = carfId,
      RCASPID = rcaspId,
      IsRCASPUser = true,
      PartyType = "Organisation",
      RCASPName = rcaspName,
      TradingName = "Uva Academy",
      TINDetails = Some(
        List(
          TinDetails(
            TINType = "UTR",
            TIN = "1111111111",
            IssuedBy = "GB"
          )
        )
      ),
      AddressDetails = fullAddress,
      PrimaryContactDetails = None,
      SecondaryContactDetails = None
    )

  private def fullOrganisationRcaspDetails(carfId: String, rcaspId: String, rcaspName: String) =
    OrganisationRcaspDetails(
      RCASPID = rcaspId,
      SubscriptionID = carfId,
      IsRCASPUser = false,
      PartyType = "Organisation",
      RCASPName = rcaspName,
      TradingName = "Uva Academy",
      TINDetails = Some(
        List(
          TinDetails(
            TINType = "UTR",
            TIN = "1111111111",
            IssuedBy = "GB"
          )
        )
      ),
      AddressDetails = fullAddress,
      PrimaryContactDetails = Some(
        RcaspContactDetails(
          ContactName = "Clavell",
          EmailAddress = "clavell@uva.edu.org",
          PhoneNumber = Some("07123412344")
        )
      ),
      SecondaryContactDetails = Some(
        RcaspContactDetails(
          ContactName = "Jacq",
          EmailAddress = "jacq@uva.edu.org",
          PhoneNumber = Some("07123412345")
        )
      )
    )

  private def emptyOptionalsOrganisationRcaspDetails(carfId: String, rcaspId: String, rcaspName: String): RcaspDetails =
    OrganisationRcaspDetails(
      RCASPID = rcaspId,
      SubscriptionID = carfId,
      IsRCASPUser = false,
      PartyType = "Organisation",
      RCASPName = rcaspName,
      TradingName = rcaspName,
      TINDetails = Some(
        List(
          TinDetails(
            TINType = "UTR",
            TIN = "1111111111",
            IssuedBy = "GB"
          )
        )
      ),
      AddressDetails = fullAddress,
      PrimaryContactDetails = Some(
        RcaspContactDetails(
          ContactName = "Clavell",
          EmailAddress = "clavell@uva.edu.org",
          PhoneNumber = None
        )
      ),
      SecondaryContactDetails = None
    )

  private def rcaspResponseCommon = RcaspResponseCommon(
    OriginatingSystem = "MDTP",
    TransmittingSystem = "EIS",
    RequestType = "VIEW",
    Regime = "CARF",
    ResponseParameters = None
  )

  private def fullAddress = RcaspAddress(
    AddressLine1 = "1 Test",
    AddressLine2 = Some("Test Street"),
    AddressLine3 = Some("Test Region"),
    AddressLine4 = Some("Testingtown"),
    PostalCode = "B23 2AZ",
    CountryCode = "GB"
  )

  private def badRequest400Response: Result =
    BadRequest(
      errorDetailJson(
        "400",
        "Invalid JSON document.",
        "instance value (\"FOO\") not found in enum (possible values: [\"BAR\"])"
      )
    )

  private def unprocessableEntity422Response =
    UnprocessableEntity(
      errorDetailJson(
        "422",
        "Unprocessable Entity",
        "999 - Unprocessable Entity"
      )
    )

  private def noRcasps422Response =
    UnprocessableEntity(
      errorDetailJson(
        "422",
        "No matching records found for the request",
        "001 - No matching records found for the request"
      )
    )

  private def internalServerError500Response: Result =
    InternalServerError(
      errorDetailJson(
        "500",
        "Internal Server Error",
        "500 - Simulated internal server error from stubs"
      )
    )

  private def serviceUnavailable503Response: Result =
    ServiceUnavailable(
      errorDetailJson(
        "503",
        "Service Unavailable",
        "503 - Simulated service unavailable from stubs"
      )
    )

  private def notAllowedResponse: Result =
    MethodNotAllowed(
      errorDetailJson(
        "405",
        "Method Not Allowed",
        "405 - Simulated method not allowed from stubs"
      )
    )

  private def forbiddenResponse: Result =
    Forbidden(
      errorDetailJson(
        "403",
        "Forbidden",
        "403 - Simulated Forbidden from stubs"
      )
    )
}
