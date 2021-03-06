package com.jetbrains.edu.learning.codeforces

import com.google.common.annotations.VisibleForTesting
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages
import com.jetbrains.edu.learning.*
import com.jetbrains.edu.learning.codeforces.CodeforcesLanguageProvider.Companion.getLanguageIdAndVersion
import com.jetbrains.edu.learning.codeforces.api.CodeforcesConnector
import com.jetbrains.edu.learning.codeforces.courseFormat.CodeforcesCourse
import com.jetbrains.edu.learning.messages.EduCoreBundle
import com.jetbrains.edu.learning.newproject.ui.JoinCourseDialogBase
import com.jetbrains.edu.learning.newproject.ui.coursePanel.CourseDisplaySettings

class StartCodeforcesContestAction : DumbAwareAction("Start Codeforces Contest") {

  override fun actionPerformed(e: AnActionEvent) {
    val course = importCodeforcesContest() ?: return
    showCourseInfo(course)
  }

  private fun showCourseInfo(course: CodeforcesCourse) {
    // EDU-2664
    // We don't provide language settings for CPP due to mess with standards
    // Decided to do it well by adding toolchain select field
    val showLanguageSettings = course.languageID != EduNames.CPP

    object : JoinCourseDialogBase(course, CourseDisplaySettings(showTagsPanel = false,
                                                                showInstructorField = false,
                                                                showLanguageSettings = showLanguageSettings)) {
      init {
        init()
      }
    }.show()
  }

  private fun importCodeforcesContest(): CodeforcesCourse? {
    val contestId = showDialogAndGetContestId() ?: return null
    val contestParameters = getContestParameters(contestId) ?: return null

    return when (val contest = getContestUnderProgress(contestParameters)) {
      is Err -> {
        showFailedToGetContestInfoNotification(contestId, contest.error)
        null
      }
      is Ok -> contest.value
    }
  }

  private fun showDialogAndGetContestId(): Int? {
    val dialog = ImportCodeforcesContestDialog()
    if (!dialog.showAndGet()) {
      return null
    }
    return dialog.getContestId()
  }

  private fun getContestParameters(contestId: Int): ContestParameters? {
    val contestInfo = getContestInfoUnderProgress(contestId)
    if (contestInfo is Err) {
      showFailedToGetContestInfoNotification(contestId, contestInfo.error)
      return null
    }
    val contest = (contestInfo as Ok).value

    val codeforcesSettings = CodeforcesSettings.getInstance()
    var contestParameters: ContestParameters?
    if (codeforcesSettings.doNotShowLanguageDialog && codeforcesSettings.isSet()) {
      contestParameters = getContestParametersFromSettings(contestId)

      if (contestParameters != null && contestParameters.codeforcesLanguageRepresentation in contest.availableLanguages) {
        return contestParameters
      }
    }

    contestParameters = showDialogAndGetContestParameters(contest)
    return contestParameters
  }

  private fun getContestParametersFromSettings(contestId: Int): ContestParameters? {
    val codeforcesSettings = CodeforcesSettings.getInstance()

    val locale = (codeforcesSettings.preferableTaskTextLanguage ?: return null).locale
    val language = codeforcesSettings.preferableLanguage ?: return null
    val languageIdAndVersion = getLanguageIdAndVersion(language) ?: return null

    return ContestParameters(contestId, locale, language, languageIdAndVersion)
  }

  private fun getContestInfoUnderProgress(contestId: Int): Result<ContestInformation, String> =
    ProgressManager.getInstance().runProcessWithProgressSynchronously<Result<ContestInformation, String>, RuntimeException>(
      {
        ProgressManager.getInstance().progressIndicator.isIndeterminate = true
        EduUtils.execCancelable {
          CodeforcesConnector.getInstance().getContestInformation(contestId)
        }
      }, EduCoreBundle.message("codeforces.getting.available.languages"), true, null)

  private fun showDialogAndGetContestParameters(contestInformation: ContestInformation): ContestParameters? {
    val contestName = contestInformation.name
    val contestLanguages = contestInformation.availableLanguages

    if (contestLanguages.isEmpty()) {
      showNoSupportedLanguagesForContestNotification(contestName)
      return null
    }

    val dialog = ChooseCodeforcesContestLanguagesDialog(contestInformation)
    if (!dialog.showAndGet()) {
      return null
    }

    val taskTextLanguage = dialog.selectedTaskTextLanguage()
    val language = dialog.selectedLanguage()
    val languageIdAndVersion = getLanguageIdAndVersion(language) ?: return null

    val codeforcesSettings = CodeforcesSettings.getInstance()
    if (dialog.isDoNotShowLanguageDialog()) {
      codeforcesSettings.preferableTaskTextLanguage = taskTextLanguage
      codeforcesSettings.preferableLanguage = language
      codeforcesSettings.doNotShowLanguageDialog = true
    }

    return ContestParameters(contestInformation.id, taskTextLanguage.locale, language, languageIdAndVersion)
  }

  private fun showFailedToGetContestInfoNotification(contestId: Int, error: String) {
    val contestUrl = CodeforcesContestConnector.getContestURLFromID(contestId)
    Messages.showErrorDialog(EduCoreBundle.message("codeforces.failed.to.get.contest.information", error.toLowerCase(), contestUrl),
                             EduCoreBundle.message("codeforces.failed.to.load.contest.title"))
  }

  private fun showNoSupportedLanguagesForContestNotification(contestName: String) {
    Messages.showErrorDialog(EduCoreBundle.message("codeforces.no.supported.languages", contestName),
                             EduCoreBundle.message("codeforces.failed.to.load.contest.title"))
  }

  companion object {
    @VisibleForTesting
    fun getContestUnderProgress(contestParameters: ContestParameters): Result<CodeforcesCourse, String> =
      ProgressManager.getInstance().runProcessWithProgressSynchronously<Result<CodeforcesCourse, String>, RuntimeException>(
        {
          ProgressManager.getInstance().progressIndicator.isIndeterminate = true
          EduUtils.execCancelable {
            CodeforcesConnector.getInstance().getContest(contestParameters)
          }
        }, EduCoreBundle.message("codeforces.getting.contest.information"), true, null)
  }
}