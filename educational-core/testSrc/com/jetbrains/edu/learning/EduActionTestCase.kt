package com.jetbrains.edu.learning

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.PsiManager
import com.intellij.testFramework.MapDataContext
import com.intellij.testFramework.TestActionEvent
import com.jetbrains.edu.coursecreator.CCStudyItemDeleteProvider
import com.jetbrains.edu.learning.courseFormat.StudyItem
import com.jetbrains.edu.learning.projectView.CourseViewPane

abstract class EduActionTestCase : EduTestCase() {

  protected fun dataContext(files: Array<VirtualFile>): MapDataContext {
    return MapDataContext().apply {
      put(CommonDataKeys.PROJECT, project)
      put(CommonDataKeys.VIRTUAL_FILE_ARRAY, files)
    }
  }

  protected fun dataContext(file: VirtualFile): MapDataContext {
    val psiManager = PsiManager.getInstance(project)
    val psiFile = psiManager.findDirectory(file) ?: psiManager.findFile(file)
    val studyItem = findStudyItem(file)
    return MapDataContext().apply {
      put(CommonDataKeys.PROJECT, project)
      put(CommonDataKeys.VIRTUAL_FILE, file)
      put(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(file))
      put(CommonDataKeys.PSI_ELEMENT, psiFile)
      put(PlatformDataKeys.DELETE_ELEMENT_PROVIDER, CCStudyItemDeleteProvider())
      if (studyItem != null) {
        put(CourseViewPane.STUDY_ITEM, studyItem)
      }
    }
  }

  protected fun dataContext(element: PsiElement): MapDataContext {
    val file = if (element is PsiFileSystemItem) element.virtualFile else element.containingFile.virtualFile
    return MapDataContext().apply {
      put(CommonDataKeys.PROJECT, project)
      put(CommonDataKeys.VIRTUAL_FILE, file)
      put(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(file))
      put(CommonDataKeys.PSI_ELEMENT, element)
      put(PlatformDataKeys.DELETE_ELEMENT_PROVIDER, CCStudyItemDeleteProvider())
    }
  }

  private fun findStudyItem(file: VirtualFile): StudyItem? {
    var studyItem: StudyItem? = null
    val course = StudyTaskManager.getInstance(project).course
    if (course != null) {
      studyItem = EduUtils.getSection(project, course, file)
      if (studyItem == null) {
        studyItem = EduUtils.getLesson(project, course, file)
      }
      if (studyItem == null) {
        studyItem = EduUtils.getTask(project, course, file)
      }
    }
    return studyItem
  }

  protected fun testAction(context: DataContext, action: AnAction, runAction: Boolean = true): Presentation {
    val e = TestActionEvent(context, action)
    action.beforeActionPerformedUpdate(e)
    if (e.presentation.isEnabledAndVisible && runAction) {
      action.actionPerformed(e)
    }
    return e.presentation
  }

  protected fun checkActionEnabled(presentation: Presentation, shouldBeEnabled: Boolean) {
    if (presentation.isEnabledAndVisible != shouldBeEnabled) {
      val message = if (shouldBeEnabled) {
        "`${presentation.text}` action is not enabled as expected"
      } else {
        "`${presentation.text}` action is not disabled as expected"
      }
      error(message)
    }
  }
}
