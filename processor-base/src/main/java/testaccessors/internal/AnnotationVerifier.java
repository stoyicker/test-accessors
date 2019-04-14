package testaccessors.internal;

import javax.lang.model.element.Element;

abstract class AnnotationVerifier<Annotation extends java.lang.annotation.Annotation> {
  final Lazy<Logger> logger;

  AnnotationVerifier(final Lazy<Logger> logger) {
    this.logger = logger;
  }

  abstract Class<Annotation> annotationClass();

  abstract boolean verify(Element element, Annotation annotation);

  final boolean verify(final Element element) {
    return verify(element, element.getAnnotation(annotationClass()));
  }
}
