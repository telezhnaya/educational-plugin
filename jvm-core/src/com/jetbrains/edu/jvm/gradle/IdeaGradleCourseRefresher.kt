package com.jetbrains.edu.jvm.gradle

import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ExternalSystemDataKeys
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode
import com.intellij.openapi.externalSystem.service.project.ExternalProjectRefreshCallback
import com.intellij.openapi.externalSystem.service.project.ProjectDataManager
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.BuildNumber
import com.intellij.util.PlatformUtils
import com.jetbrains.edu.learning.EduCourseBuilder
import com.jetbrains.edu.learning.RefreshCause
import com.jetbrains.edu.learning.isUnitTestMode
import com.jetbrains.edu.learning.projectView.CourseViewPane
import org.jetbrains.plugins.gradle.util.GradleConstants

class IdeaGradleCourseRefresher : GradleCourseRefresher {
  override fun isAvailable(): Boolean = PlatformUtils.isIntelliJ()

  override fun refresh(project: Project, cause: RefreshCause) {
    // Gradle projects are refreshed by IDEA itself on (re)opening since 2019.3
    // BACKCOMPAT: 2019.2
    if (cause == RefreshCause.PROJECT_CREATED && ApplicationInfo.getInstance().build >= BUILD_193 && !isUnitTestMode) return

    val projectBasePath = project.basePath ?: return

    val builder = ImportSpecBuilder(project, GradleConstants.SYSTEM_ID)
      .use(ProgressExecutionMode.IN_BACKGROUND_ASYNC)
      .dontReportRefreshErrors()

    builder.useDefaultCallback()

    // Build toolwindow will be opened if `ExternalSystemDataKeys.NEWLY_IMPORTED_PROJECT` is true while sync
    project.putUserData(ExternalSystemDataKeys.NEWLY_IMPORTED_PROJECT, null)
    ExternalSystemUtil.refreshProject(projectBasePath, builder.build())
    if (!isUnitTestMode) {
      ExternalSystemUtil.invokeLater(project, ModalityState.NON_MODAL) {
        ProjectView.getInstance(project).changeViewCB(CourseViewPane.ID, null)
      }
    }
  }

  companion object {
    // BACKCOMPAT: 2019.3
    private val BUILD_193: BuildNumber = BuildNumber.fromString("193")!!
  }
}
