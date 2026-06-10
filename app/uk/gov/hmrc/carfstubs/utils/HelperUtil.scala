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

package uk.gov.hmrc.carfstubs.utils

import play.api.libs.json.{JsObject, Json}

object HelperUtil {
  def errorDetailJson(errorCode: String, errorMessage: String, sourceFaultDetailMessage: String): JsObject =
    Json.obj(
      "errorDetail" -> Json.obj(
        "correlationId"     -> "d60de98c-f499-47f5-b2d6-e80966e8d19e",
        "errorCode"         -> errorCode,
        "errorMessage"      -> errorMessage,
        "source"            -> "carf-stubs",
        "sourceFaultDetail" -> Json.obj(
          "detail" -> Json.arr(sourceFaultDetailMessage)
        ),
        "timestamp"         -> java.time.Instant.now().toString
      )
    )
}
