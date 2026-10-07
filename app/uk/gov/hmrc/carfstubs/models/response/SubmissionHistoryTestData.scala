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

package uk.gov.hmrc.carfstubs.models.response

import uk.gov.hmrc.carfstubs.models.response.SubmissionHistoryStatus.{Failed, Passed, Pending}

import java.time.{Instant, ZoneId}
import java.time.temporal.ChronoUnit

object SubmissionHistoryTestData {

  private val ukZoneId: ZoneId = ZoneId.of("Europe/London")

  def submissionsListFewRecords(now: Instant): Seq[SubmissionHistoryRecord] =
    Seq(
      SubmissionHistoryRecord(
        rcaspId = "ZMCAR0123456789",
        rcaspName = "Nemona Champion",
        filename = "filename1.xml",
        submissionStatus = Passed,
        uploadDateTime = now.minus(1, ChronoUnit.DAYS),
        messageRefId = "MSG-2024-0001",
        submissionFileType = "CARF-701",
        reportingYear = now.minus(1, ChronoUnit.DAYS).atZone(ukZoneId).getYear.toString,
        submissionCaseId = "CARF-SUB-001"
      ),
      SubmissionHistoryRecord(
        rcaspId = "ZMCAR0123456789",
        rcaspName = "Nemona Champion",
        filename = "filename2.xml",
        submissionStatus = Failed,
        uploadDateTime = now.minus(2, ChronoUnit.DAYS),
        messageRefId = "MSG-2024-0002",
        submissionFileType = "CARF-701",
        reportingYear = now.minus(2, ChronoUnit.DAYS).atZone(ukZoneId).getYear.toString,
        submissionCaseId = "CARF-SUB-002"
      ),
      SubmissionHistoryRecord(
        rcaspId = "ZMCAR0123456789",
        rcaspName = "Nemona Champion",
        filename = "filename3.xml",
        submissionStatus = Passed,
        uploadDateTime = now.minus(28, ChronoUnit.DAYS).minus(1, ChronoUnit.MINUTES),
        messageRefId = "MSG-2024-0003",
        submissionFileType = "CARF-703",
        reportingYear = now.minus(28, ChronoUnit.DAYS).minus(1, ChronoUnit.MINUTES).atZone(ukZoneId).getYear.toString,
        submissionCaseId = "CARF-SUB-003"
      ),
      SubmissionHistoryRecord(
        rcaspId = "ZMCAR0123456789",
        rcaspName = "Nemona Champion",
        filename = "filename4.xml",
        submissionStatus = Passed,
        uploadDateTime = now.minus(40, ChronoUnit.DAYS),
        messageRefId = "MSG-2024-0003",
        submissionFileType = "CARF-702",
        reportingYear = now.minus(40, ChronoUnit.DAYS).atZone(ukZoneId).getYear.toString,
        submissionCaseId = "CARF-SUB-004"
      ),
      SubmissionHistoryRecord(
        rcaspId = "ZMCAR0123456780",
        rcaspName = "Other RCASP Ltd",
        filename = "filename5.xml",
        submissionStatus = Passed,
        uploadDateTime = now.minus(32, ChronoUnit.DAYS),
        messageRefId = "MSG-2024-0005",
        submissionFileType = "CARF-702",
        reportingYear = now.minus(32, ChronoUnit.DAYS).atZone(ukZoneId).getYear.toString,
        submissionCaseId = "CARF-SUB-005"
      ),
      SubmissionHistoryRecord(
        rcaspId = "ZMCAR0123456780",
        rcaspName = "Other RCASP Ltd",
        filename = "filename6.xml",
        submissionStatus = Pending,
        uploadDateTime = now.minus(33, ChronoUnit.DAYS),
        messageRefId = "MSG-2024-0006",
        submissionFileType = "CARF-702",
        reportingYear = now.minus(33, ChronoUnit.DAYS).atZone(ukZoneId).getYear.toString,
        submissionCaseId = "CARF-SUB-006"
      ),
      SubmissionHistoryRecord(
        rcaspId = "ZMCAR0123456780",
        rcaspName = "Other RCASP Ltd",
        filename = "filename7.xml",
        submissionStatus = Failed,
        uploadDateTime = now.minus(34, ChronoUnit.DAYS),
        messageRefId = "MSG-2024-0007",
        submissionFileType = "CARF-702",
        reportingYear = now.minus(34, ChronoUnit.DAYS).atZone(ukZoneId).getYear.toString,
        submissionCaseId = "CARF-SUB-007"
      )
    )

  def submissionsList70Records(now: Instant): Seq[SubmissionHistoryRecord] =
    Seq(
      SubmissionHistoryRecord(
        rcaspId = "ZMCAR0123456789",
        rcaspName = "Nemona Champion",
        filename = "filename1.xml",
        submissionStatus = Passed,
        uploadDateTime = now.minus(1, ChronoUnit.DAYS),
        messageRefId = "MSG-2024-0001",
        submissionFileType = "CARF-701",
        reportingYear = now.minus(1, ChronoUnit.DAYS).atZone(ukZoneId).getYear.toString,
        submissionCaseId = "CARF-SUB-001"
      )
    ) ++
      (1 to 70).map(_ + 28).map { daysAgo =>
        SubmissionHistoryRecord(
          rcaspId = "ZMCAR0123456780",
          rcaspName = "Other RCASP Ltd",
          filename = s"filename$daysAgo.xml",
          submissionStatus = Passed,
          uploadDateTime = now.minus(daysAgo, ChronoUnit.DAYS),
          messageRefId = s"MSG-2024-00$daysAgo",
          submissionFileType = "CARF-701",
          reportingYear = now.minus(daysAgo, ChronoUnit.DAYS).atZone(ukZoneId).getYear.toString,
          submissionCaseId = s"CARF-SUB-0$daysAgo"
        )
      } ++
      (1 to 5).map(_ + 28).map { daysAgo =>
        SubmissionHistoryRecord(
          rcaspId = "ZMCAR0123456780",
          rcaspName = "Other RCASP Ltd",
          filename = s"filename$daysAgo-pending.xml",
          submissionStatus = Pending,
          uploadDateTime = now.minus(daysAgo, ChronoUnit.DAYS),
          messageRefId = s"MSG-2024-00$daysAgo",
          submissionFileType = "CARF-701",
          reportingYear = now.minus(daysAgo, ChronoUnit.DAYS).atZone(ukZoneId).getYear.toString,
          submissionCaseId = s"CARF-SUB-0$daysAgo"
        )
      }

  def submissionsList1000Records(now: Instant): Seq[SubmissionHistoryRecord] =
    (1 to 1000).reverse.map(_ + 28).map { daysAgo =>
      SubmissionHistoryRecord(
        rcaspId = "ZMCAR0123456780",
        rcaspName = "Other RCASP Ltd",
        filename = s"filename$daysAgo.xml",
        submissionStatus = Passed,
        uploadDateTime = now.minus(daysAgo, ChronoUnit.DAYS),
        messageRefId = s"MSG-2024-$daysAgo",
        submissionFileType = "CARF-701",
        reportingYear = now.minus(daysAgo, ChronoUnit.DAYS).atZone(ukZoneId).getYear.toString,
        submissionCaseId = s"CARF-SUB-$daysAgo"
      )
    }
}
