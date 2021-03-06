package com.jetbrains.edu.coursecreator.actions

import com.intellij.openapi.util.text.StringUtil
import com.jetbrains.edu.learning.courseFormat.*
import com.jetbrains.edu.learning.messages.EduCoreBundle
import icons.EducationalCoreIcons.Lesson

class CCCreateLesson : CCCreateLessonBase<Lesson>(StudyItemType.LESSON, Lesson) {

  override val studyItemVariants: List<StudyItemVariant>
    get() = listOf(
      StudyItemVariant(StringUtil.toTitleCase(EduCoreBundle.message("study.item.lesson")), "", Lesson, ::Lesson),
      StudyItemVariant(StringUtil.toTitleCase(EduCoreBundle.message("study.item.framework.lesson")), "", Lesson, ::FrameworkLesson)
    )
}
