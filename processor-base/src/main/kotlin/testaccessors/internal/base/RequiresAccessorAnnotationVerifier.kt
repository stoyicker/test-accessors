package testaccessors.internal.base

import testaccessors.RequiresAccessor
import java.util.*
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier

class RequiresAccessorAnnotationVerifier(logger: Logger)
  : AnnotationVerifier<RequiresAccessor>(logger) {

  override fun annotationClass() = RequiresAccessor::class.java

  override fun verify(element: Element, annotation: RequiresAccessor) =
      element.run {
        when {
          isPublicAndFinal() -> {
            logger.error("Fields that are public and non-final are guaranteed to be freely manipulated from anywhere, so you don't need to generate accessors for them.", element)
            false
          }
          isPropertyWithoutBackingField() -> {
            logger.error("Properties without backing fields are not supported.", element)
            false
          }
          else -> {
            true
          }
        }
      }

  private fun Element.isPublicAndFinal() = modifiers.run { contains(Modifier.PUBLIC) && !contains(Modifier.FINAL) }

  private fun Element.isPropertyWithoutBackingField() = run {
    kind != ElementKind.FIELD && enclosingElement.enclosedElements.any {
      it.simpleName.toString().toLowerCase(Locale.ENGLISH).contentEquals(this.simpleName.substring(3).toLowerCase(Locale.ENGLISH))
    }
  }
}
