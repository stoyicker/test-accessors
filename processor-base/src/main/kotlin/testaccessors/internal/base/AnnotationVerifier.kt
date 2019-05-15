package testaccessors.internal.base

import javax.lang.model.element.Element

abstract class AnnotationVerifier<Annotation : kotlin.Annotation>(val logger: Logger) {
  abstract fun annotationClass(): Class<Annotation>

  abstract fun verify(
      element: Element,
      annotation: Annotation = element.getAnnotation<Annotation>(annotationClass())): Boolean
}
