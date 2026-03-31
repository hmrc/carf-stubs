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
import uk.gov.hmrc.carfstubs.models.request.Subscription

trait SubscriptionHelper extends Logging {

  def returnResponse(request: Subscription): Result = {

    logger.info(s" Received subscription request: \n -> ${Json.prettyPrint(Json.toJson(request))}")

    val organisationName            = request.primaryContact.organisation.map(_.name).getOrElse("")
    val primaryContactNameOrOrgName = request.primaryContact.individual.map(_.firstName).getOrElse(organisationName)

    primaryContactNameOrOrgName match {
      case "duplicateSubmission"        => duplicateSubmission004Response
      case "duplicateAlreadyRegistered" => alreadyRegistered007Response
      case "alreadyRegistered"          => alreadyRegistered400Response
      case "invalid"                    => requestCouldNotBeProcessed003Response
      case "internalServerError"        => internalServerError500Response
      case "badRequest"                 => badRequest400Response
      case "serviceUnavailable"         => serviceUnavailable503Response
      case "noBusinessPartner"          => noBusinessPartner008Response
      case "invalidType"                => invalidIdType015Response
      case _                            =>
        val secondaryContactOrgName     = request.secondaryContact.flatMap(_.organisation.map(_.name))
        val primaryContactNameOrOrgName = request.primaryContact.individual.map(_.lastName).getOrElse(organisationName)

        secondaryContactOrgName.getOrElse(primaryContactNameOrOrgName).takeRight(2) match {
          case "XX" => createSubResponseWithEnrolBadRequest(request)
          case "YY" => createSubResponseWithEnrolInternalError(request)
          case _    => createSubscriptionResponse(request)
        }
    }
  }

  private def createSubscriptionResponse(request: Subscription) =
    createSuccessfulSubResponse(s"XCARF${request.idNumber.slice(2, 10)}")

  private def createSubResponseWithEnrolBadRequest(request: Subscription) =
    createSuccessfulSubResponse(s"WCARF${request.idNumber.slice(2, 10)}")

  private def createSubResponseWithEnrolInternalError(request: Subscription) =
    createSuccessfulSubResponse(s"YCARF${request.idNumber.slice(2, 10)}")

  private def createSuccessfulSubResponse(carfReference: String) =
    Ok(
      Json.obj(
        "success" -> Json.obj(
          "CARFReference"  -> carfReference,
          "processingDate" -> java.time.Instant.now().toString
        )
      )
    )

  private def alreadyRegistered400Response: Result =
    UnprocessableEntity(
      Json.obj(
        "errorDetail" -> Json.obj(
          "correlationId"     -> "1ae81b45-41b4-4642-ae1c-db1126900001",
          "errorCode"         -> "422",
          "errorMessage"      -> "Business Error (from backend)",
          "source"            -> "Backend",
          "sourceFaultDetail" -> Json.obj(
            "detail" -> Json.arr("Business Error (from backend)")
          ),
          "timestamp"         -> java.time.Instant.now().toString
        )
      )
    )

  private def noBusinessPartner008Response: Result =
    UnprocessableEntity(
      Json.obj(
        "errorDetail" -> Json.obj(
          "correlationId"     -> "f058ebd6-02f7-4d3f-942e-904344e8cde5",
          "errorCode"         -> "422",
          "errorMessage"      -> "No Business Partner identified for ID provided",
          "source"            -> "Backend",
          "sourceFaultDetail" -> Json.obj(
            "detail" -> Json.arr("008 - No Business Partner identified for ID provided")
          ),
          "timestamp"         -> java.time.Instant.now().toString
        )
      )
    )

  private def duplicateSubmission004Response: Result =
    UnprocessableEntity(
      Json.obj(
        "errorDetail" -> Json.obj(
          "correlationId"     -> "f058ebd6-02f7-4d3f-942e-904344e8cde5",
          "errorCode"         -> "004",
          "errorMessage"      -> "Duplicate submission acknowledgment reference",
          "source"            -> "Backend",
          "sourceFaultDetail" -> Json.obj(
            "detail" -> Json.arr("004 - Duplicate submission acknowledgment reference")
          ),
          "timestamp"         -> java.time.Instant.now().toString
        )
      )
    )

  private def invalidIdType015Response: Result =
    UnprocessableEntity(
      Json.obj(
        "errorDetail" -> Json.obj(
          "correlationId"     -> "f058ebd6-02f7-4d3f-942e-904344e8cde5",
          "errorCode"         -> "015",
          "errorMessage"      -> "Invalid ID type",
          "source"            -> "Backend",
          "sourceFaultDetail" -> Json.obj(
            "detail" -> Json.arr("015 - Invalid ID type")
          ),
          "timestamp"         -> java.time.Instant.now().toString
        )
      )
    )

  private def requestCouldNotBeProcessed003Response: Result =
    InternalServerError(
      Json.obj(
        "errorDetail" -> Json.obj(
          "correlationId"     -> "f058ebd6-02f7-4d3f-942e-904344e8cde5",
          "errorCode"         -> "003",
          "errorMessage"      -> "Request could not be processed",
          "source"            -> "Backend",
          "sourceFaultDetail" -> Json.obj(
            "detail" -> Json.arr("003 - Request could not be processed")
          ),
          "timestamp"         -> java.time.Instant.now().toString
        )
      )
    )

  private def alreadyRegistered007Response: Result =
    UnprocessableEntity(
      Json.obj(
        "errorDetail" -> Json.obj(
          "correlationId"     -> "f058ebd6-02f7-4d3f-942e-904344e8cde5",
          "errorCode"         -> "007",
          "errorMessage"      -> "Business Partner already has a Subscription for this regime ",
          "source"            -> "Backend",
          "sourceFaultDetail" -> Json.obj(
            "detail" -> Json.arr("007 - Business Partner already has a Subscription for this regime ")
          ),
          "timestamp"         -> java.time.Instant.now().toString
        )
      )
    )

  private def badRequest400Response: Result =
    BadRequest(
      Json.obj(
        "errorDetail" -> Json.obj(
          "correlationId"     -> "d60de98c-f499-47f5-b2d6-e80966e8d19e",
          "errorCode"         -> "400",
          "errorMessage"      -> "Bad Request",
          "source"            -> "carf-stubs",
          "sourceFaultDetail" -> Json.obj(
            "detail" -> Json.arr("400 - Simulated bad request from stubs")
          ),
          "timestamp"         -> java.time.Instant.now().toString
        )
      )
    )

  private def internalServerError500Response: Result =
    InternalServerError(
      Json.obj(
        "errorDetail" -> Json.obj(
          "correlationId"     -> "d60de98c-f499-47f5-b2d6-e80966e8d19e",
          "errorCode"         -> "500",
          "errorMessage"      -> "Internal Server Error",
          "source"            -> "carf-stubs",
          "sourceFaultDetail" -> Json.obj(
            "detail" -> Json.arr("500 - Simulated internal server error from stubs")
          ),
          "timestamp"         -> java.time.Instant.now().toString
        )
      )
    )

  private def serviceUnavailable503Response: Result =
    ServiceUnavailable(
      Json.obj(
        "errorDetail" -> Json.obj(
          "correlationId"     -> "d60de98c-f499-47f5-b2d6-e80966e8d19e",
          "errorCode"         -> "503",
          "errorMessage"      -> "Service Unavailable",
          "source"            -> "carf-stubs",
          "sourceFaultDetail" -> Json.obj(
            "detail" -> Json.arr("503 - Simulated service unavailable from stubs")
          ),
          "timestamp"         -> java.time.Instant.now().toString
        )
      )
    )

}
