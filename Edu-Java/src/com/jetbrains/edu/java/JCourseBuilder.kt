package com.jetbrains.edu.java

import com.jetbrains.edu.jvm.gradle.GradleCourseBuilderBase

open class JCourseBuilder : GradleCourseBuilderBase() {

  override val buildGradleTemplateName: String = JAVA_BUILD_GRADLE_TEMPLATE_NAME
  override val taskTemplateName: String = JConfigurator.TASK_JAVA
  override val mainTemplateName: String = JConfigurator.MAIN_JAVA
  override val testTemplateName: String = JConfigurator.TEST_JAVA
  override fun getLanguageSettings() = JLanguageSettings()

  companion object {
    private const val JAVA_BUILD_GRADLE_TEMPLATE_NAME = "java-build.gradle"
  }
}
