package testaccessors.internal.base

import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class Logger(private val messager: Messager) {
  fun error(message: CharSequence, culprit: Element? = null) =
    messager.printMessage(Diagnostic.Kind.ERROR, message, culprit)

  fun warn(message: CharSequence, culprit: Element? = null) =
    messager.printMessage(Diagnostic.Kind.WARNING, message, culprit)
}
