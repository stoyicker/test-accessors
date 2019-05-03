package testaccessors.internal

import javax.annotation.processing.Filer
import javax.lang.model.element.Element
import javax.lang.model.element.PackageElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

abstract class AbstractAccessorWriter(
    val elementUtils: Elements,
    val typeUtils: Types,
    val options: Options) {

  fun nameForGeneratedClassFrom(enclosingClassSimpleNames: List<String>) =
      "${enclosingClassSimpleNames.joinToString("")}TestAccessors"

  fun extractLocation(element: Element): Array<String> =
      (element.enclosingElement?.let { extractLocation(it) } ?: emptyArray()) +
          (arrayOf((element as? PackageElement)?.qualifiedName?.toString()
              ?: element.simpleName.toString()))

  abstract fun writeAccessorClass(annotatedElements: Set<Element>, filer: Filer)

  companion object {
    const val PARAMETER_NAME_NEW_VALUE = "newValue"
    const val TYPE_NAME_VALUE = "TestAccessorsValue"
    const val ERROR_MESSAGE_ILLEGAL_ACCESS = "Accessing this method from this source set is not allowed"
  }
}
