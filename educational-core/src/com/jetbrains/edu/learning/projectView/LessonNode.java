package com.jetbrains.edu.learning.projectView;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.jetbrains.edu.learning.courseFormat.Lesson;
import com.jetbrains.edu.learning.courseFormat.tasks.Task;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LessonNode extends EduNode {
  @NotNull protected final Project myProject;
  protected final ViewSettings myViewSettings;
  @NotNull protected final Lesson myLesson;

  public LessonNode(@NotNull Project project,
                    PsiDirectory value,
                    ViewSettings viewSettings,
                    @NotNull Lesson lesson) {
    super(project, value, viewSettings);
    myProject = project;
    myViewSettings = viewSettings;
    myLesson = lesson;
  }

  @Override
  protected void updateImpl(@NotNull PresentationData data) {
    updatePresentation(myLesson, data);
  }

  @Override
  public int getWeight() {
    return myLesson.getIndex();
  }

  @Nullable
  @Override
  protected AbstractTreeNode modifyChildNode(@NotNull AbstractTreeNode child) {
    Object value = child.getValue();
    if (value instanceof PsiDirectory) {
      PsiDirectory directory = (PsiDirectory)value;
      Task task = myLesson.getTask(directory.getName());
      if (task == null) {
        return null;
      }
      PsiDirectory taskDirectory = CourseViewUtils.findTaskDirectory(myProject, directory, task);
      if (taskDirectory == null) return null;
      return createTaskNode(taskDirectory, task);
    }
    return null;
  }

  @NotNull
  protected TaskNode createTaskNode(PsiDirectory directory, Task task) {
    return new TaskNode(myProject, directory, myViewSettings, task);
  }

  @NotNull
  public Lesson getLesson() {
    return myLesson;
  }
}
