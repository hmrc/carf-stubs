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
import uk.gov.hmrc.carfstubs.models.request.CreateRCASPRequest
import uk.gov.hmrc.carfstubs.models.response.*
import uk.gov.hmrc.carfstubs.utils.HelperUtil.errorDetailJson

trait RcaspHelper extends Logging {

  def returnRcaspResponse(carfId: String): Result =
    carfId.take(2).toUpperCase match {
      case "YY" => internalServerError500Response
      case "TT" => badRequest400Response
      case "SS" => serviceUnavailable503Response
      case "RR" => Ok(Json.toJson(fullOrganisationRcaspResponse(carfId)))
      case "PP" => unprocessableEntity422Response
      case "OO" => Ok(Json.toJson(emptyOptionalsOrganisationRcaspResponse(carfId)))
      case "NN" => Ok(Json.toJson(multipleOrganisationRcaspResponse(carfId)))
      case "MM" => Ok(Json.toJson(emptyOptionalsIndividualRcaspResponse(carfId)))
      case "LL" => Ok(Json.toJson(multipleIndividualRcaspResponse(carfId)))
      case "KK" => Ok(Json.toJson(noRcaspsResponse))
      case _    => Ok(Json.toJson(fullIndividualRcaspResponse(carfId)))
    }

  def returnCreateResponse(request: CreateRCASPRequest): Result =
    request.RCASPManagement.RequestDetails.PrimaryContactDetails.fold(badRequest400Response) { contactDetails =>
      contactDetails.EmailAddress.take(2) match {
        case "UU" => unprocessableEntity422Response
        case "VV" => forbiddenResponse
        case "WW" => notAllowedResponse
        case "XX" => badRequest400Response
        case "YY" => internalServerError500Response
        case "ZZ" => serviceUnavailable503Response
        case _    => successfulCreateResponse
      }
    }

  private def successfulCreateResponse: Result =
    Ok(
      Json.toJson(
        SubmitRcaspResponse(
          SubmitResponseDetails(
            SubmitReturnParameters("RCASPID", "RCASP12345")
          )
        )
      )
    )

  private def fullIndividualRcaspResponse(carfId: String) = ViewRcaspResponse(
    ViewRCASP = ViewRcasp(
      ResponseCommon = rcaspResponseCommon,
      ResponseDetails =
        RcaspResponseDetails(RCASPList = List(fullIndividualRcaspDetails(carfId, rcaspId = "RCASP56789")))
    )
  )

  private def emptyOptionalsIndividualRcaspResponse(carfId: String) = ViewRcaspResponse(
    ViewRCASP = ViewRcasp(
      ResponseCommon = rcaspResponseCommon,
      ResponseDetails =
        RcaspResponseDetails(RCASPList = List(emptyOptionalsIndividualRcaspDetails(carfId, rcaspId = "RCASP45678")))
    )
  )

  private def multipleIndividualRcaspResponse(carfId: String) = ViewRcaspResponse(
    ViewRCASP = ViewRcasp(
      ResponseCommon = rcaspResponseCommon,
      ResponseDetails = RcaspResponseDetails(RCASPList =
        List(
          fullIndividualRcaspDetails(carfId, rcaspId = "RCASP56789"),
          emptyOptionalsIndividualRcaspDetails(carfId, rcaspId = "RCASP45678")
        )
      )
    )
  )

  private def noRcaspsResponse = ViewRcaspResponse(
    ViewRCASP = ViewRcasp(
      ResponseCommon = rcaspResponseCommon,
      ResponseDetails = RcaspResponseDetails(RCASPList = List.empty)
    )
  )

  private def fullIndividualRcaspDetails(carfId: String, rcaspId: String) = IndividualRcaspDetails(
    SubscriptionID = carfId,
    RCASPID = rcaspId,
    IsRCASPUser = true,
    PartyType = "Individual",
    FirstName = "Penny",
    LastName = "Cassiopeia",
    TINDetails = Some(
      List(
        TinDetails(
          TINType = "UTR",
          TIN = "6893649",
          IssuedBy = "GB"
        )
      )
    ),
    AddressDetails = fullAddress,
    PrimaryContactDetails = Some(
      RcaspContactDetails(
        ContactName = "Penny Cassiopeia",
        EmailAddress = "penny.cassiopeia@uva.edu.org",
        PhoneNumber = Some("07123412345")
      )
    )
  )

  private def emptyOptionalsIndividualRcaspDetails(carfId: String, rcaspId: String) = IndividualRcaspDetails(
    SubscriptionID = carfId,
    RCASPID = rcaspId,
    IsRCASPUser = false,
    PartyType = "Individual",
    FirstName = "Nemona",
    LastName = "Champion",
    TINDetails = None,
    AddressDetails = fullAddress,
    PrimaryContactDetails = None
  )

  private def fullOrganisationRcaspResponse(carfId: String) = ViewRcaspResponse(
    ViewRCASP = ViewRcasp(
      ResponseCommon = rcaspResponseCommon,
      ResponseDetails =
        RcaspResponseDetails(RCASPList = List(fullOrganisationRcaspDetails(carfId, rcaspId = "RCASP12345")))
    )
  )

  private def emptyOptionalsOrganisationRcaspResponse(carfId: String) = ViewRcaspResponse(
    ViewRCASP = ViewRcasp(
      ResponseCommon = rcaspResponseCommon,
      ResponseDetails =
        RcaspResponseDetails(RCASPList = List(emptyOptionalsOrganisationRcaspDetails(carfId, rcaspId = "RCASP23456")))
    )
  )

  private def multipleOrganisationRcaspResponse(carfId: String) = ViewRcaspResponse(
    ViewRCASP = ViewRcasp(
      ResponseCommon = rcaspResponseCommon,
      ResponseDetails = RcaspResponseDetails(RCASPList =
        List(
          fullOrganisationRcaspDetails(carfId, rcaspId = "RCASP12345"),
          emptyOptionalsOrganisationRcaspDetails(carfId, rcaspId = "RCASP23456")
        )
      )
    )
  )

  private def fullOrganisationRcaspDetails(carfId: String, rcaspId: String) = OrganisationRcaspDetails(
    SubscriptionID = carfId,
    RCASPID = rcaspId,
    IsRCASPUser = true,
    PartyType = "Organisation",
    RCASPName = "Mesagoza",
    TradingName = "Uva Academy",
    TINDetails = Some(
      List(
        TinDetails(
          TINType = "UTR",
          TIN = "6893649",
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

  private def emptyOptionalsOrganisationRcaspDetails(carfId: String, rcaspId: String) = OrganisationRcaspDetails(
    SubscriptionID = carfId,
    RCASPID = rcaspId,
    IsRCASPUser = false,
    PartyType = "Organisation",
    RCASPName = "Amazon UK",
    TradingName = "Tools for Traders Limited",
    TINDetails = None,
    AddressDetails = fullAddress,
    PrimaryContactDetails = None,
    SecondaryContactDetails = None
  )

  private def rcaspResponseCommon = RcaspResponseCommon(
    OriginatingSystem = "CADX",
    TransmittingSystem = "EIS",
    RequestType = "VIEW",
    Regime = "CARF",
    ResponseParameters = None
  )

  private def fullAddress = RcaspAddress(
    AddressLine1 = "2 High Street",
    AddressLine2 = Some("Birmingham"),
    AddressLine3 = Some("Nowhereshire"),
    AddressLine4 = Some("Down the road"),
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
