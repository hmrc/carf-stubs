/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.carfstubs.utils

import uk.gov.hmrc.domain.{Generator, SaUtrGenerator}

import scala.util.Random

trait IdGenerators {

  val rand                   = new scala.util.Random
  val randomisedNino: String = new Generator().nextNino.toString()
  val randomisedUtr: String  = new SaUtrGenerator().nextSaUtr.toString()

  // prefixes
  val ctutr                  = "111"
  val internalServerErrorUtr = "990"
  val badRequestUtr          = "991"
  val serviceUnavailableUtr  = "992"
  val notProcessableUtr      = "993"
  val notFoundUtr            = "994"

  def generateUtr(prefix: String): String =
    prefix + randomisedUtr.substring(3)

  val individualNino          = "AA1"
  val internalServerErrorNino = "CIS"
  val badRequestNino          = "CBR"
  val serviceUnavailableNino  = "CSU"
  val notProcessableNino      = "CNP"
  val notFoundNino            = "CNF"

  def generateNino(prefix: String): String =
    prefix + randomisedNino.substring(3)

  val validSafeId                  = "XE9"
  val validSafeId2                 = "XE2"
  val validSafeId1                 = "XE1"
  val validSafeId0                 = "XE0"
  val idToMatchEnrolmentKey        = "XE3"
  val displayIndSub                = "XIS"
  val displayOrgSub                = "XOS"
  val displaySubNoSecondContact    = "XWS"
  val fatcaEnrolmentWithGroupIds   = "XWG"
  val fatcaEnrolmentWithNoGroupIds = "XNG"

  def generateId(prefix: String): String = {
    val rand   = new Random()
    val chars  = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val digits = "0123456789"

    val prefixSection = if (prefix.isEmpty) {
      (1 to 3)
        .map(
          _ => chars(rand.nextInt(chars.length))
        )
        .mkString
    } else {
      prefix.take(3).toUpperCase
    }

    val lettersSection = (1 to 2)
      .map(
        _ => chars(rand.nextInt(chars.length))
      )
      .mkString
    val digitsSection = (1 to 10)
      .map(
        _ => digits(rand.nextInt(digits.length))
      )
      .mkString

    prefixSection + lettersSection + digitsSection
  }

}
