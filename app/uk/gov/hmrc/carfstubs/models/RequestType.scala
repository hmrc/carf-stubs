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

package uk.gov.hmrc.carfstubs.models

sealed trait RequestType {
  val name: String
  val functionName: String
  def printFunctionName: String = s"${name.toLowerCase}$functionName"
}
case class Create(functionName: String) extends RequestType {
  override val name: String = "Create"
}

case class Update(functionName: String) extends RequestType {
  override val name: String = "Update"
}

case class Delete(functionName: String) extends RequestType {
  override val name: String = "Delete"
}
