package com.jetbrains.edu.coursecreator.ui

import com.intellij.ide.ui.newItemPopup.NewItemPopupUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.InputValidatorEx
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.util.text.StringUtil
import com.jetbrains.edu.coursecreator.CCStudyItemPathInputValidator
import com.jetbrains.edu.coursecreator.actions.NewStudyItemInfo
import com.jetbrains.edu.coursecreator.actions.NewStudyItemUiModel
import com.jetbrains.edu.learning.EduExperimentalFeatures
import com.jetbrains.edu.learning.courseFormat.Course
import com.jetbrains.edu.learning.isFeatureEnabled
import com.jetbrains.edu.learning.messages.EduCoreBundle

fun createNewStudyItemUi(
  dialogGenerator: (Project, Course, NewStudyItemUiModel, List<AdditionalPanel>) -> CCCreateStudyItemDialogBase
): NewStudyItemUi {
  return if (isFeatureEnabled(EduExperimentalFeatures.NEW_ITEM_POPUP_UI)) NewStudyItemPopupUi() else NewStudyItemDialogUi(dialogGenerator)
}

class NewStudyItemPopupUi : NewStudyItemUi {
  override fun show(
    project: Project,
    course: Course,
    model: NewStudyItemUiModel,
    additionalPanels: List<AdditionalPanel>,
    studyItemCreator: (NewStudyItemInfo) -> Unit
  ) {
    val validator = CCStudyItemPathInputValidator(course, model.itemType, model.parentDir)
    val popup = createLightWeightPopup(model, validator, studyItemCreator)
    popup.showCenteredInCurrentWindow(project)
  }

  private fun createLightWeightPopup(model: NewStudyItemUiModel, validator: InputValidatorEx, studyItemCreator: (NewStudyItemInfo) -> Unit): JBPopup {
    val contentPanel = NewStudyItemPopupPanel(model.itemType, model.studyItemVariants)
    val nameField = contentPanel.textField
    nameField.text = model.suggestedName
    nameField.selectAll()
    val title = EduCoreBundle.message("action.new.study.item.title", StringUtil.toTitleCase(model.itemType.presentableName))
    val popup = NewItemPopupUtil.createNewItemPopup(title, contentPanel, nameField)
    contentPanel.setApplyAction { event ->
      val name = nameField.text
      if (validator.checkInput(name) && validator.canClose(name)) {
        popup.closeOk(event)
        val itemProducer = contentPanel.getSelectedItem()?.producer
        if (itemProducer != null) {
          val info = NewStudyItemInfo(nameField.text, model.baseIndex + CCItemPositionPanel.AFTER_DELTA, itemProducer)
          studyItemCreator(info)
        }
      }
      else {
        val errorMessage = validator.getErrorText(name)
        contentPanel.setError(errorMessage)
      }
    }

    return popup
  }
}
