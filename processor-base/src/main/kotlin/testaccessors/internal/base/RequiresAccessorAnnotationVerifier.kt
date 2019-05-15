package testaccessors.internal.base

import testaccessors.RequiresAccessor
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier

class RequiresAccessorAnnotationVerifier(logger: Logger)
  : AnnotationVerifier<RequiresAccessor>(logger) {

  override fun annotationClass() = RequiresAccessor::class.java

  override fun verify(element: Element, annotation: RequiresAccessor) =
      element.modifiers.run {
        if (contains(Modifier.PUBLIC) && !contains(Modifier.FINAL)) {
          logger.warn("Fields that are public and non-final are guaranteed to be accessible from anywhere at anytime, so you don't want to generate accessors for them.", element)
          false
        } else {
          true
        }
      }
}
