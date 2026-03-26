/*
 * Copyright 2025 HM Revenue & Customs
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
import play.api.mvc.Results.{BadRequest, Forbidden, InternalServerError, NotFound, Ok, ServiceUnavailable, UnprocessableEntity}
import play.api.mvc.{Result, Results}
import uk.gov.hmrc.carfstubs.models.request.{RegisterWithIDRequest, RegisterWithoutIdRequest}
import uk.gov.hmrc.carfstubs.models.response.*

import java.time.LocalDate
import scala.util.Random

trait RegistrationHelper extends Logging {

  private sealed trait JourneyType

  private case object UserEntryOrg extends JourneyType
  private case object AutoMatchOrg extends JourneyType
  private case object IndWithNino extends JourneyType
  private case object IndWithUtr extends JourneyType

  def returnResponse(request: RegisterWithIDRequest): Result = {
    val idNumber = request.requestDetail.IDNumber
    val idType   = request.requestDetail.IDType

    val journeyType: JourneyType = request.requestDetail.individual match {
      case Some(value) => if (idType == "UTR") IndWithUtr else IndWithNino
      case None        => request.requestDetail.organisation.fold(AutoMatchOrg)(_ => UserEntryOrg)
    }

    def getCodeFromOrgType(journeyType: JourneyType): Option[String] =
      journeyType match {
        case UserEntryOrg => Some("0000")
        case _            => None
      }

    (idNumber.take(1), journeyType) match {
      case ("9" | "Y", _) => InternalServerError(Json.toJson(errorDetail500Response))
      case ("8" | "X", _) => NotFound("The match was unsuccessful")

      case ("7", UserEntryOrg | AutoMatchOrg)                              =>
        Ok(Json.toJson(createEmptyOrganisationResponse(request, getCodeFromOrgType(journeyType))))
      case ("7", IndWithUtr) | ("W", IndWithNino)                          => Ok(Json.toJson(createEmptyIndividualResponse(request)))
      case ("6", UserEntryOrg | AutoMatchOrg) if idNumber.startsWith("68") =>
        Ok(Json.toJson(createOrgResponseWithInvalidCode(request, getCodeFromOrgType(journeyType))))
      case ("6", UserEntryOrg | AutoMatchOrg)                              =>
        Ok(Json.toJson(createNonUkOrganisationResponse(request, getCodeFromOrgType(journeyType))))
      case ("6", IndWithUtr)                                               =>
        Ok(Json.toJson(createNonUkIndividualResponse(request, getCodeFromOrgType(journeyType))))
      case ("5" | "T", _)                                                  => UnprocessableEntity(Json.toJson(errorDetail422Response))
      case ("4" | "S", _)                                                  => ServiceUnavailable(Json.toJson(errorDetail503Response))
      case (_, UserEntryOrg | AutoMatchOrg)                                => Ok(Json.toJson(createFullOrganisationResponse(request)))
      // TODO: in future, split out IndWithUtr and IndWithNino to remove getOrElse in createFullIndividualResponse
      case (_, IndWithUtr | IndWithNino)                                   => Ok(Json.toJson(createFullIndividualResponse(request)))

      case _ => BadRequest(Json.toJson(errorDetail400Response))
    }
  }

  def returnResponseWithoutId(request: RegisterWithoutIdRequest): Result = {
    val thingToMatchOn = request.registerWithoutIDRequest.requestDetail.individual match {
      case Some(value) => value.firstName
      case None        => request.registerWithoutIDRequest.requestDetail.organisation.get.organisationName
    }

    thingToMatchOn.take(1).toUpperCase match {
      case "Y" =>
        val body = Json.toJson(
          ErrorResponse(
            ErrorDetail(
              correlationId = java.util.UUID.randomUUID().toString,
              timestamp = LocalDate.now().toString,
              errorCode = "500",
              errorMessage = "Unexpected error",
              sourceFaultDetail = SourceFaultDetail(detail = List("Internal server error occurred"))
            )
          )
        )
        logger.info(s"Stub Response Body \n-> ${Json.prettyPrint(body)}")
        InternalServerError(body)
      case "X" =>
        val body = Json.toJson(
          ErrorResponse(
            ErrorDetail(
              correlationId = java.util.UUID.randomUUID().toString,
              timestamp = LocalDate.now().toString,
              errorCode = "422",
              errorMessage = "The match was unsuccessful",
              sourceFaultDetail = SourceFaultDetail(detail = List("No matching record found"))
            )
          )
        )
        logger.info(s"Stub Response Body \n-> ${Json.prettyPrint(body)}")
        UnprocessableEntity(body)
      case "Z" =>
        val body = Json.toJson(
          ErrorResponse(
            ErrorDetail(
              correlationId = java.util.UUID.randomUUID().toString,
              timestamp = LocalDate.now().toString,
              errorCode = "400",
              errorMessage = "Bad Request",
              sourceFaultDetail = SourceFaultDetail(detail = List("Invalid JSON document."))
            )
          )
        )
        logger.info(s"Stub Response Body \n-> ${Json.prettyPrint(body)}")
        BadRequest(body)
      case "S" =>
        val body = Json.toJson(
          ErrorResponse(
            ErrorDetail(
              correlationId = java.util.UUID.randomUUID().toString,
              timestamp = LocalDate.now().toString,
              errorCode = "503",
              errorMessage = "Service unavailable",
              sourceFaultDetail = SourceFaultDetail(detail = List("External service unavailable"))
            )
          )
        )
        logger.info(s"Stub Response Body \n-> ${Json.prettyPrint(body)}")
        ServiceUnavailable(body)

      case "F" =>
        logger.info(s"Stub Response body is 'Forbidden'")
        Forbidden("Forbidden")
      case _   =>
        val body = Json.toJson(createFullResponseWithoutId)
        logger.info(s"Stub Response Body \n-> ${Json.prettyPrint(body)}")
        Ok(body)
    }
  }

  private def createFullOrganisationResponse(request: RegisterWithIDRequest): RegisterWithIDResponse =
    RegisterWithIDResponse(
      responseCommon = ResponseCommon(
        processingDate = LocalDate.now().toString,
        returnParameters = None,
        status = "OK",
        statusText = None
      ),
      responseDetail = Some(
        ResponseDetail(
          ARN = "",
          SAFEID = generateSafeId,
          address = fullAddress,
          contactDetails = ContactDetails(None, None, None, None),
          individual = None,
          isAnASAgent = Some(false),
          isAnAgent = false,
          isAnIndividual = false,
          isEditable = false,
          organisation = Some(
            OrganisationResponse(
              organisationName =
                request.requestDetail.organisation.map(_.organisationName).getOrElse("AutoMatched Org Ltd"),
              code = request.requestDetail.organisation.map(_.organisationType),
              isAGroup = false,
              organisationType = request.requestDetail.organisation.map(_.organisationType)
            )
          )
        )
      )
    )

  private def createEmptyOrganisationResponse(
      request: RegisterWithIDRequest,
      code: Option[String]
  ): RegisterWithIDResponse =
    RegisterWithIDResponse(
      responseCommon = ResponseCommon(
        processingDate = LocalDate.now().toString,
        returnParameters = None,
        status = "OK",
        statusText = None
      ),
      responseDetail = Some(
        ResponseDetail(
          ARN = "",
          SAFEID = generateSafeId,
          address = emptyAddress,
          contactDetails = ContactDetails(None, None, None, None),
          individual = None,
          isAnASAgent = None,
          isAnAgent = false,
          isAnIndividual = false,
          isEditable = false,
          organisation = Some(
            OrganisationResponse(
              organisationName = "Apples and Pears LTD",
              // For User Entry Org this should always be populated
              code = code,
              isAGroup = false,
              organisationType = None
            )
          )
        )
      )
    )

  private def createNonUkOrganisationResponse(
      request: RegisterWithIDRequest,
      code: Option[String]
  ): RegisterWithIDResponse =
    RegisterWithIDResponse(
      responseCommon = ResponseCommon(
        processingDate = LocalDate.now().toString,
        returnParameters = None,
        status = "OK",
        statusText = None
      ),
      responseDetail = Some(
        ResponseDetail(
          ARN = "",
          SAFEID = generateSafeId,
          address = randomiseNonUkAddress,
          contactDetails = ContactDetails(None, None, None, None),
          individual = None,
          isAnASAgent = Some(false),
          isAnAgent = false,
          isAnIndividual = false,
          isEditable = false,
          organisation = Some(
            OrganisationResponse(
              organisationName = request.requestDetail.organisation.map(_.organisationName).getOrElse("Outside Org"),
              code = code,
              isAGroup = false,
              organisationType = request.requestDetail.organisation.map(_.organisationType)
            )
          )
        )
      )
    )

  private def createOrgResponseWithInvalidCode(
      request: RegisterWithIDRequest,
      code: Option[String]
  ): RegisterWithIDResponse =
    RegisterWithIDResponse(
      responseCommon = ResponseCommon(
        processingDate = LocalDate.now().toString,
        returnParameters = None,
        status = "OK",
        statusText = None
      ),
      responseDetail = Some(
        ResponseDetail(
          ARN = "",
          SAFEID = generateSafeId,
          address = addressWithInvalidCountryCode,
          contactDetails = ContactDetails(None, None, None, None),
          individual = None,
          isAnASAgent = Some(false),
          isAnAgent = false,
          isAnIndividual = false,
          isEditable = false,
          organisation = Some(
            OrganisationResponse(
              organisationName = request.requestDetail.organisation.map(_.organisationName).getOrElse("Outside Org"),
              code = code,
              isAGroup = false,
              organisationType = request.requestDetail.organisation.map(_.organisationType)
            )
          )
        )
      )
    )

  private def createFullIndividualResponse(request: RegisterWithIDRequest): RegisterWithIDResponse =
    RegisterWithIDResponse(
      responseCommon = ResponseCommon(
        processingDate = LocalDate.now().toString,
        returnParameters = Some(List(ReturnParameters(paramName = "Test-ParamName", paramValue = "Test-ParamValue"))),
        status = "OK",
        statusText = Some("Test-StatusText")
      ),
      responseDetail = Some(
        ResponseDetail(
          ARN = "Test-ARN",
          SAFEID = generateSafeId,
          address = fullAddress,
          contactDetails = ContactDetails(
            emailAddress = Some("test@example.com"),
            faxNumber = Some("Test-FaxNo"),
            mobileNumber = Some("Test-MobileNo"),
            phoneNumber = Some("TestPhoneNo")
          ),
          individual = Some(
            IndividualResponse(
              dateOfBirth = request.requestDetail.individual.flatMap(_.dateOfBirth),
              firstName = request.requestDetail.individual.map(_.firstName).getOrElse("Ind First Name"),
              lastName = request.requestDetail.individual.map(_.lastName).getOrElse("Ind Last Name"),
              middleName = Some("Bjorn")
            )
          ),
          isAnASAgent = Some(false),
          isAnAgent = request.requestDetail.isAnAgent,
          isAnIndividual = true,
          isEditable = false,
          organisation = None
        )
      )
    )

  private def createEmptyIndividualResponse(request: RegisterWithIDRequest): RegisterWithIDResponse =
    RegisterWithIDResponse(
      responseCommon = ResponseCommon(
        processingDate = LocalDate.now().toString,
        returnParameters = None,
        status = "OK",
        statusText = None
      ),
      responseDetail = Some(
        ResponseDetail(
          ARN = "Test-ARN",
          SAFEID = generateSafeId,
          address = emptyAddress,
          contactDetails = ContactDetails(
            emailAddress = None,
            faxNumber = None,
            mobileNumber = None,
            phoneNumber = None
          ),
          individual = Some(
            IndividualResponse(
              dateOfBirth = None,
              firstName = "Apple",
              lastName = "Pear",
              middleName = None
            )
          ),
          isAnASAgent = None,
          isAnAgent = request.requestDetail.isAnAgent,
          isAnIndividual = true,
          isEditable = false,
          organisation = None
        )
      )
    )

  private def createNonUkIndividualResponse(
      request: RegisterWithIDRequest,
      code: Option[String]
  ): RegisterWithIDResponse =
    RegisterWithIDResponse(
      responseCommon = ResponseCommon(
        processingDate = LocalDate.now().toString,
        returnParameters = None,
        status = "OK",
        statusText = None
      ),
      responseDetail = Some(
        ResponseDetail(
          ARN = "",
          SAFEID = generateSafeId,
          address = randomiseNonUkAddress,
          contactDetails = ContactDetails(None, None, None, None),
          individual = Some(
            IndividualResponse(
              dateOfBirth = request.requestDetail.individual.get.dateOfBirth,
              firstName = request.requestDetail.individual.get.firstName,
              lastName = request.requestDetail.individual.get.lastName,
              middleName = None
            )
          ),
          isAnASAgent = Some(false),
          isAnAgent = false,
          isAnIndividual = false,
          isEditable = false,
          organisation = Some(
            OrganisationResponse(
              organisationName = request.requestDetail.organisation.map(_.organisationName).getOrElse("Outside Org"),
              code = code,
              isAGroup = false,
              organisationType = request.requestDetail.organisation.map(_.organisationType)
            )
          )
        )
      )
    )

  private val createFullResponseWithoutId: RegisterWithoutIDResponse =
    RegisterWithoutIDResponse(registerWithoutIDResponse =
      RegisterWithoutIDResponseDetails(
        responseCommon = ResponseCommon(
          processingDate = LocalDate.now().toString,
          returnParameters = None,
          status = "OK",
          statusText = None
        ),
        responseDetail = ResponseDetailWithoutId(
          SAFEID = generateSafeId
        )
      )
    )

  private def fullAddress = AddressResponse(
    addressLine1 = "2 High Street",
    addressLine2 = Some("Birmingham"),
    addressLine3 = Some("Nowhereshire"),
    addressLine4 = Some("Down the road"),
    postalCode = Some("B23 2AZ"),
    countryCode = "GB"
  )

  private def emptyAddress = AddressResponse(
    addressLine1 = "2 Newarre Road",
    addressLine2 = None,
    addressLine3 = None,
    addressLine4 = None,
    postalCode = None,
    countryCode = "GB"
  )

  private def nonUkAddressUs = AddressResponse(
    addressLine1 = "123 Big Apple",
    addressLine2 = Some("New York"),
    addressLine3 = None,
    addressLine4 = None,
    postalCode = Some("BNY 2AZ"),
    countryCode = "US"
  )

  private def nonUkAddressFrance = AddressResponse(
    addressLine1 = "123 France street",
    addressLine2 = Some("Paris"),
    addressLine3 = None,
    addressLine4 = None,
    postalCode = Some("FRY 2AZ"),
    countryCode = "FR"
  )

  private def nonUkAddressGermany = AddressResponse(
    addressLine1 = "123 Germany street",
    addressLine2 = Some("Frankfurt"),
    addressLine3 = None,
    addressLine4 = None,
    postalCode = Some("DEY 2AZ"),
    countryCode = "DE"
  )

  private def nonUkAddressSwitzerland = AddressResponse(
    addressLine1 = "123 Switzerland street",
    addressLine2 = Some("Zurich"),
    addressLine3 = None,
    addressLine4 = None,
    postalCode = Some("CH 2AZ"),
    countryCode = "CH"
  )

  private def nonUkAddressJersey = AddressResponse(
    addressLine1 = "123 Jersey street",
    addressLine2 = Some("Jersey"),
    addressLine3 = None,
    addressLine4 = None,
    postalCode = Some("JE4 1AA"),
    countryCode = "JE"
  )

  private def addressWithInvalidCountryCode = AddressResponse(
    addressLine1 = "123 Bad street",
    addressLine2 = Some("CountryCodeBad"),
    addressLine3 = None,
    addressLine4 = None,
    postalCode = Some("ZX 2AZ"),
    countryCode = "ZX"
  )

  private def errorDetail400Response = ErrorResponse(
    errorDetail = ErrorDetail(
      correlationId = java.util.UUID.randomUUID().toString,
      timestamp = LocalDate.now().toString,
      errorCode = "400",
      errorMessage = "Unhandled or invalid scenario.",
      sourceFaultDetail = SourceFaultDetail(
        detail = List("Unhandled or invalid scenario.")
      )
    )
  )

  private def errorDetail422Response = ErrorResponse(
    errorDetail = ErrorDetail(
      correlationId = java.util.UUID.randomUUID().toString,
      timestamp = LocalDate.now().toString,
      errorCode = "422",
      errorMessage = "Unprocessable entity",
      sourceFaultDetail = SourceFaultDetail(
        detail = List("Unprocessable entity")
      )
    )
  )

  private def errorDetail500Response = ErrorResponse(
    errorDetail = ErrorDetail(
      correlationId = java.util.UUID.randomUUID().toString,
      timestamp = LocalDate.now().toString,
      errorCode = "500",
      errorMessage = "Unexpected error",
      sourceFaultDetail = SourceFaultDetail(
        detail = List("Unexpected error")
      )
    )
  )

  private def errorDetail503Response = ErrorResponse(
    errorDetail = ErrorDetail(
      correlationId = java.util.UUID.randomUUID().toString,
      timestamp = LocalDate.now().toString,
      errorCode = "503",
      errorMessage = "Service unavailable",
      sourceFaultDetail = SourceFaultDetail(
        detail = List("Service unavailable")
      )
    )
  )

  private def randomiseNonUkAddress = {
    val countries = List(
      nonUkAddressUs,
      nonUkAddressFrance,
      nonUkAddressGermany,
      nonUkAddressSwitzerland,
      nonUkAddressJersey
    )
    val index     = Random.between(0, countries.size)
    countries(index)
  }

  private def generateSafeId: String = {
    val random        = new Random()
    val randomInteger = (0 to 9).map(_ => random.between(0, 9)).mkString

    s"XE$randomInteger"
  }
}
