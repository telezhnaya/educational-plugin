package com.jetbrains.edu.rust

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.text.VersionComparatorUtil
import com.jetbrains.edu.learning.EduCourseBuilder
import com.jetbrains.edu.learning.configuration.EduConfiguratorWithSubmissions
import com.jetbrains.edu.learning.pluginVersion
import com.jetbrains.edu.rust.checker.RsTaskCheckerProvider
import org.rust.cargo.CargoConstants
import org.rust.ide.icons.RsIcons
import org.rust.lang.RsConstants
import javax.swing.Icon

class RsConfigurator : EduConfiguratorWithSubmissions<RsProjectSettings>() {
  override val taskCheckerProvider: RsTaskCheckerProvider
    get() = RsTaskCheckerProvider()

  override val testFileName: String
    get() = ""

  override fun getMockFileName(text: String): String = RsConstants.MAIN_RS_FILE

  override val courseBuilder: EduCourseBuilder<RsProjectSettings>
    get() = RsCourseBuilder()

  override val testDirs: List<String>
    get() = listOf("tests")

  override val sourceDir: String
    get() = "src"

  override val logo: Icon
    get() = RsIcons.RUST

  override fun excludeFromArchive(project: Project, file: VirtualFile): Boolean {
    return super.excludeFromArchive(project, file) || file.name == CargoConstants.LOCK_FILE ||
           generateSequence(file, VirtualFile::getParent).any { it.name == CargoConstants.ProjectLayout.target }
  }

  override val isEnabled: Boolean
    get() {
      val rustPluginVersion = pluginVersion("org.rust.lang") ?: return false
      // Rust plugin has incompatibility in API that we use before 0.2.106
      // so disable Rust support for all versions below 0.2.106
      return VersionComparatorUtil.compare(rustPluginVersion, "0.2.106") >= 0
    }
}
