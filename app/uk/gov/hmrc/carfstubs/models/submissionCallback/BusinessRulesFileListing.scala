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

package uk.gov.hmrc.carfstubs.models.submissionCallback

import uk.gov.hmrc.carfstubs.models.response.FileListing

object BusinessRulesFileListing {

  val businessRulesFilesInBackend: Seq[FileListing] = Seq(
    FileListing(
      filename = "BusinessRuleCheckSampleRequest_ValidFile_v0.3.xml",
      fileSize = 792L,
      downloadURL = "data/examples/aeoi/BusinessRuleCheckSampleRequest_ValidFile_v0.3.xml",
      metadata = Seq.empty
    ),
    FileListing(
      filename = "BusinessRuleCheckSampleRequest_validFile_with_150errors.xml",
      fileSize = 35081L,
      downloadURL = "data/examples/aeoi/BusinessRuleCheckSampleRequest_validFile_with_150errors.xml",
      metadata = Seq.empty
    ),
    FileListing(
      filename = "BusinessRuleCheckSampleRequest_validFile_with_errors.xml",
      fileSize = 1136L,
      downloadURL = "data/examples/aeoi/BusinessRuleCheckSampleRequest_validFile_with_errors.xml",
      metadata = Seq.empty
    ),
    FileListing(
      filename = "BusinessRuleCheckSampleRequest_invalidFile_schema__errors.xml",
      fileSize = 1036L,
      downloadURL = "data/examples/aeoi/BusinessRuleCheckSampleRequest_invalidFile_schema__errors.xml",
      metadata = Seq.empty
    ),
    FileListing(
      filename = "malformed-xml.xml",
      fileSize = 56L,
      downloadURL = "data/examples/malformed-xml.xml",
      metadata = Seq.empty
    ),
    FileListing(
      filename = "unknown.xml",
      fileSize = 100L,
      downloadURL = "data/examples/aeoi/unknown.xml",
      metadata = Seq.empty
    )
  )

}
