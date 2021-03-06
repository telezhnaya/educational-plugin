package com.jetbrains.edu.rust

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.io.exists
import com.jetbrains.edu.coursecreator.actions.NewStudyItemInfo
import com.jetbrains.edu.coursecreator.actions.StudyItemType
import com.jetbrains.edu.coursecreator.actions.TemplateFileInfo
import com.jetbrains.edu.learning.EduCourseBuilder
import com.jetbrains.edu.learning.LanguageSettings
import com.jetbrains.edu.learning.RefreshCause
import com.jetbrains.edu.learning.StudyTaskManager
import com.jetbrains.edu.learning.courseFormat.Course
import com.jetbrains.edu.learning.newproject.CourseProjectGenerator
import org.rust.cargo.CargoConstants
import org.rust.cargo.project.model.CargoProject
import org.rust.cargo.project.model.cargoProjects
import org.rust.ide.newProject.RsPackageNameValidator
import org.rust.lang.RsConstants
import org.rust.openapiext.pathAsPath
import java.nio.file.Path

class RsCourseBuilder : EduCourseBuilder<RsProjectSettings> {

    override fun getCourseProjectGenerator(course: Course): CourseProjectGenerator<RsProjectSettings>? =
        RsCourseProjectGenerator(this, course)

    override fun getLanguageSettings(): LanguageSettings<RsProjectSettings> = RsLanguageSettings()

    override fun refreshProject(project: Project, cause: RefreshCause) {
        val course = StudyTaskManager.getInstance(project).course ?: return
        if (project.isSingleWorkspaceProject) {
            refreshWorkspace(project)
        } else {
            refreshTaskPackages(project, course)
        }
    }

    // If there is `Cargo.toml` in root of project, we assume that all task packages are in single workspace.
    // In this case it's enough just to refresh all current cargo projects (actually, single one)
    // or delegate project discovering to IntelliJ Rust plugin when there isn't any cargo project yet
    private fun refreshWorkspace(project: Project) {
        val cargoProjects = project.cargoProjects
        if (!cargoProjects.hasAtLeastOneValidProject) {
            cargoProjects.discoverAndRefresh()
        } else {
            cargoProjects.refreshAllProjects()
        }
    }

    private fun refreshTaskPackages(project: Project, course: Course) {
        val cargoProjects = project.cargoProjects
        val cargoProjectMap = HashMap<VirtualFile, CargoProject>()
        val toAttach = mutableListOf<Path>()
        val toDetach = mutableListOf<CargoProject>()
        for (cargoProject in cargoProjects.allProjects) {
            val rootDir = cargoProject.rootDir
            // we should check existence of manifest file because after study item rename
            // manifest path will be outdated
            if (rootDir == null || !cargoProject.manifest.exists()) {
                toDetach += cargoProject
            } else {
                cargoProjectMap[rootDir] = cargoProject
            }
        }

        course.visitLessons {
            for (task in it.taskList) {
                val taskDir = task.getTaskDir(project) ?: continue
                val cargoProject = cargoProjectMap[taskDir]
                if (cargoProject == null) {
                    val manifestFile = taskDir.findChild(CargoConstants.MANIFEST_FILE) ?: continue
                    toAttach.add(manifestFile.pathAsPath)
                }
            }
        }

        toDetach.forEach { cargoProjects.detachCargoProject(it) }
        // TODO: find out way not to refresh all projects on each `CargoProjectsService.attachCargoProject` call.
        //  Now it leads to O(n^2) cargo invocations
        toAttach.forEach { cargoProjects.attachCargoProject(it) }
        cargoProjects.refreshAllProjects()
    }

    override fun getTestTaskTemplates(course: Course, info: NewStudyItemInfo, withSources: Boolean): List<TemplateFileInfo> {
        val templates = mutableListOf<TemplateFileInfo>()
        if (withSources) {
            templates += TemplateFileInfo(LIB_RS, "src/$LIB_RS", true)
            templates +=TemplateFileInfo(MAIN_RS, "src/$MAIN_RS", true)
            templates +=TemplateFileInfo(CargoConstants.MANIFEST_FILE, CargoConstants.MANIFEST_FILE, true)
        }
        templates += TemplateFileInfo(TESTS_RS, "tests/$TESTS_RS", false)
        return templates
    }

    override fun getExecutableTaskTemplates(course: Course, info: NewStudyItemInfo, withSources: Boolean): List<TemplateFileInfo> {
        if (!withSources) return emptyList()
        return listOf(
          TemplateFileInfo(MAIN_RS, "src/$MAIN_RS", true),
          TemplateFileInfo(CargoConstants.MANIFEST_FILE, CargoConstants.MANIFEST_FILE, true)
        )
    }

    override fun extractInitializationParams(project: Project, info: NewStudyItemInfo): Map<String, String> {
        return mapOf("PACKAGE_NAME" to info.name.toPackageName())
    }

    override fun validateItemName(name: String, itemType: StudyItemType): String? =
      if (itemType == StudyItemType.TASK) RsPackageNameValidator.validate(name.toPackageName(), true) else null

    companion object {
        private const val LIB_RS = RsConstants.LIB_RS_FILE
        private const val MAIN_RS = RsConstants.MAIN_RS_FILE
        private const val TESTS_RS = "tests.rs"
    }
}
