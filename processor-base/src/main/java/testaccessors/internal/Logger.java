package testaccessors.internal;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

final class Logger {
  private final Messager messager;

  Logger(final Messager messager) {
    this.messager = messager;
  }

  final void error(CharSequence message) {
    error(message, null);
  }

  final void error(CharSequence message, Element culprit) {
    messager.printMessage(Diagnostic.Kind.ERROR, message, culprit);
  }

  final void warn(CharSequence message) {
    warn(message, null);
  }

  final void warn(CharSequence message, Element culprit) {
    messager.printMessage(Diagnostic.Kind.WARNING, message, culprit);
  }
}
