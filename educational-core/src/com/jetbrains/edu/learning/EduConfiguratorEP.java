package com.jetbrains.edu.learning;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.AbstractExtensionPointBean;
import com.intellij.openapi.util.AtomicNotNullLazyValue;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.NotNull;

public class EduConfiguratorEP extends AbstractExtensionPointBean {
  public static final String EP_NAME = "Educational.configurator";

  @Attribute("implementationClass")
  public String implementationClass;

  @Attribute("language")
  public String language = "";

  @Attribute("courseType")
  public String courseType = EduNames.PYCHARM;

  private final AtomicNotNullLazyValue<EduConfigurator> myInstanceHolder = new AtomicNotNullLazyValue<EduConfigurator>() {
    @NotNull
    @Override
    protected EduConfigurator compute() {
      try {
        return instantiate(implementationClass, ApplicationManager.getApplication().getPicoContainer());
      }
      catch (final ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  };

  @NotNull
  public EduConfigurator getInstance() {
    return myInstanceHolder.getValue();
  }
}