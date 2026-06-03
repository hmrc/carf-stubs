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
import uk.gov.hmrc.carfstubs.models.response.*

trait RcaspHelper extends Logging {

  def returnRcaspResponse(carfId: String, rcaspId: String): Result = {
    logger.info(s"Received view RCASP request")

    carfId.take(1).toUpperCase match {
      case "Y" => internalServerError500Response
      case "T" => badRequest400Response
      case "S" => serviceUnavailable503Response
      case "R" => Ok(Json.toJson(fullOrganisationRcaspResponse(carfId, rcaspId)))
      case "P" => unprocessableEntity422Response
      case "O" => Ok(Json.toJson(emptyOptionalsOrganisationRcaspResponse(carfId, rcaspId)))
      case "N" => Ok(Json.toJson(multipleOrganisationRcaspResponse(carfId, rcaspId)))
      case "M" => Ok(Json.toJson(emptyOptionalsIndividualRcaspResponse(carfId, rcaspId)))
      case "L" => Ok(Json.toJson(multipleIndividualRcaspResponse(carfId, rcaspId)))
      case _   => Ok(Json.toJson(fullIndividualRcaspResponse(carfId, rcaspId)))
    }
  }

  private def fullIndividualRcaspResponse(carfId: String, rcaspId: String) = ViewRcaspResponse(
    ViewRCASP = ViewRcasp(
      ResponseCommon = rcaspResponseCommon,
      ResponseDetails = RcaspResponseDetails(RCASPList = List(fullIndividualRcaspDetails(carfId, rcaspId)))
    )
  )

  private def emptyOptionalsIndividualRcaspResponse(carfId: String, rcaspId: String) = ViewRcaspResponse(
    ViewRCASP = ViewRcasp(
      ResponseCommon = rcaspResponseCommon,
      ResponseDetails = RcaspResponseDetails(RCASPList = List(emptyOptionalsIndividualRcaspDetails(carfId, rcaspId)))
    )
  )

  private def multipleIndividualRcaspResponse(carfId: String, rcaspId: String) = ViewRcaspResponse(
    ViewRCASP = ViewRcasp(
      ResponseCommon = rcaspResponseCommon,
      ResponseDetails = RcaspResponseDetails(RCASPList =
        List(fullIndividualRcaspDetails(carfId, rcaspId), emptyOptionalsIndividualRcaspDetails(carfId, rcaspId))
      )
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
      RcaspContact(
        ContactName = "Penny Cassiopeia",
        EmailAddress = "penny.cassiopeia@uva.edu.org",
        PhoneNumber = "07123412345"
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

  private def fullOrganisationRcaspResponse(carfId: String, rcaspId: String) = ViewRcaspResponse(
    ViewRCASP = ViewRcasp(
      ResponseCommon = rcaspResponseCommon,
      ResponseDetails = RcaspResponseDetails(RCASPList = List(fullOrganisationRcaspDetails(carfId, rcaspId)))
    )
  )

  private def emptyOptionalsOrganisationRcaspResponse(carfId: String, rcaspId: String) = ViewRcaspResponse(
    ViewRCASP = ViewRcasp(
      ResponseCommon = rcaspResponseCommon,
      ResponseDetails = RcaspResponseDetails(RCASPList = List(emptyOptionalsOrganisationRcaspDetails(carfId, rcaspId)))
    )
  )

  private def multipleOrganisationRcaspResponse(carfId: String, rcaspId: String) = ViewRcaspResponse(
    ViewRCASP = ViewRcasp(
      ResponseCommon = rcaspResponseCommon,
      ResponseDetails = RcaspResponseDetails(RCASPList =
        List(fullOrganisationRcaspDetails(carfId, rcaspId), emptyOptionalsOrganisationRcaspDetails(carfId, rcaspId))
      )
    )
  )

  private def fullOrganisationRcaspDetails(carfId: String, rcaspId: String) = OrganisationRcaspDetails(
    SubscriptionID = carfId,
    RCASPID = rcaspId,
    IsRCASPUser = true,
    PartyType = "Organisation",
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
      RcaspContact(
        ContactName = "Clavell",
        EmailAddress = "clavell@uva.edu.org",
        PhoneNumber = "07123412344"
      )
    ),
    SecondaryContactDetails = Some(
      RcaspContact(
        ContactName = "Jacq",
        EmailAddress = "jacq@uva.edu.org",
        PhoneNumber = "07123412345"
      )
    )
  )

  private def emptyOptionalsOrganisationRcaspDetails(carfId: String, rcaspId: String) = OrganisationRcaspDetails(
    SubscriptionID = carfId,
    RCASPID = rcaspId,
    IsRCASPUser = false,
    PartyType = "Organisation",
    TradingName = "Uva Academy",
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

  private def fullAddress = AddressResponse(
    addressLine1 = "2 High Street",
    addressLine2 = Some("Birmingham"),
    addressLine3 = Some("Nowhereshire"),
    addressLine4 = Some("Down the road"),
    postalCode = Some("B23 2AZ"),
    countryCode = "GB"
  )

  private def badRequest400Response: Result =
    BadRequest(
      Json.obj(
        "errorDetail" -> Json.obj(
          "correlationId"     -> "1ae81b45-41b4-4642-ae1c-db1126900001",
          "errorCode"         -> "400",
          "errorMessage"      -> "Invalid JSON document.",
          "source"            -> "journey-ite202-service-camel",
          "sourceFaultDetail" -> Json.obj(
            "detail" -> Json.arr("instance value (\\\"FOO\\\") not found in enum (possible values: [\\\"BAR\\\"])")
          ),
          "timestamp"         -> java.time.Instant.now().toString
        )
      )
    )

  private def unprocessableEntity422Response: Result =
    UnprocessableEntity(
      Json.obj(
        "errorDetail" -> Json.obj(
          "correlationId"     -> "1ae81b45-41b4-4642-ae1c-db1126900001",
          "errorCode"         -> "422",
          "errorMessage"      -> "No matching records found for the request",
          "source"            -> "Backend",
          "sourceFaultDetail" -> Json.obj(
            "detail" -> Json.arr("001 - No matching records found for the request")
          ),
          "timestamp"         -> java.time.Instant.now().toString
        )
      )
    )

  private def internalServerError500Response: Result =
    InternalServerError(
      Json.obj(
        "errorDetail" -> Json.obj(
          "correlationId"     -> "1ae81b45-41b4-4642-ae1c-db1126900001",
          "errorCode"         -> "500",
          "errorMessage"      -> "<detail as generated by service>",
          "source"            -> "journey-<journey-name>-service-camel",
          "sourceFaultDetail" -> Json.obj(
            "detail" -> Json.arr("<detail as generated by service>")
          ),
          "timestamp"         -> java.time.Instant.now().toString
        )
      )
    )

  private def serviceUnavailable503Response: Result =
    ServiceUnavailable(
      Json.obj(
        "errorDetail" -> Json.obj(
          "correlationId"     -> "1ae81b45-41b4-4642-ae1c-db1126900001",
          "errorCode"         -> "503",
          "errorMessage"      -> "<detail as generated by service>",
          "source"            -> "journey-<journey-name>-service-camel",
          "sourceFaultDetail" -> Json.obj(
            "detail" -> Json.arr("<detail as generated by service>")
          ),
          "timestamp"         -> java.time.Instant.now().toString
        )
      )
    )
}
