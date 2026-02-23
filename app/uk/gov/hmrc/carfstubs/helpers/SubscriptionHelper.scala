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

    val organisationName   = request.primaryContact.organisation.map(_.name).getOrElse("")
    val primaryContactName = request.primaryContact.individual.map(_.firstName).getOrElse(organisationName)
    val idNumber           = request.idNumber.take(3)

    (primaryContactName, idNumber) match {
      case ("duplicateSubmission", _)        => duplicateSubmission004Response
      case ("duplicateAlreadyRegistered", _) => alreadyRegistered007Response
      case ("alreadyRegistered", _)          => alreadyRegistered400Response
      case ("invalid", _)                    => requestCouldNotBeProcessed003Response
      case ("internalServerError", _)        => internalServerError500Request
      case (_, "XE3")                        => noBusinessPartnerResponse
      case (_, "XWG" | "XM0")                => createSubscriptionResponse(request)
      case (_, "XID")                        => invalidIdType015Response
      case _                                 => createSubscriptionResponse(request)

    }
  }

  private def createSubscriptionResponse(request: Subscription) =
    Created(
      Json.obj(
        "success" -> Json.obj(
          "CARFReference"  -> s"XCARF${request.idNumber.drop(2)}",
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

  private def noBusinessPartnerResponse: Result =
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

  private def internalServerError500Request: Result =
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

}
