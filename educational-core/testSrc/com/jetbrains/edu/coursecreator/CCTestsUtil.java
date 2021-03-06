package com.jetbrains.edu.coursecreator;

import com.google.common.collect.Collections2;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.edu.learning.courseFormat.AnswerPlaceholder;

import java.util.Collection;
import java.util.List;

public class CCTestsUtil {
  public static final String BEFORE_POSTFIX = "_before.txt";
  public static final String AFTER_POSTFIX = "_after.txt";

  private CCTestsUtil() {
  }

  public static boolean comparePlaceholders(AnswerPlaceholder p1, AnswerPlaceholder p2) {
    if (p1.getOffset() != p2.getOffset()) return false;
    if (p1.getLength() != p2.getLength()) return false;
    if (p1.getPossibleAnswerLength() != p2.getPossibleAnswerLength()) return false;
    if (p1.getPossibleAnswer() != null ? !p1.getPossibleAnswer().equals(p2.getPossibleAnswer()) : p2.getPossibleAnswer() != null) return false;
    if (p1.getPlaceholderText() != null ? !p1.getPlaceholderText().equals(p2.getPlaceholderText()) : p2.getPlaceholderText() != null) return false;
    if (!p1.getHints().equals(p2.getHints())) return false;
    return true;
  }

  public static String getPlaceholderPresentation(AnswerPlaceholder placeholder) {
    return "offset=" + placeholder.getOffset() +
           " length=" + placeholder.getLength() +
           " possibleAnswer=" + placeholder.getPossibleAnswer() +
           " placeholderText=" + placeholder.getPlaceholderText();
  }

  public static String getPlaceholdersPresentation(List<AnswerPlaceholder> placeholders) {
    Collection<String> transformed = Collections2.transform(placeholders, placeholder -> getPlaceholderPresentation(placeholder));
    return "[" + StringUtil.join(transformed, ",") + "]";
  }
}
