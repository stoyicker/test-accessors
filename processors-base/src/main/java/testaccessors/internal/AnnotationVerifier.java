package testaccessors.internal;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;

abstract class AnnotationVerifier<Annotation extends java.lang.annotation.Annotation> {
    final Logger logger;

    AnnotationVerifier(final Messager messager, final LogLevel logLevel) {
        logger = new Logger(messager, logLevel);
    }

    abstract Class<Annotation> annotationClass();

    abstract boolean verify(Element element, Annotation annotation);

    final boolean verify(final Element element) {
        return verify(element, element.getAnnotation(annotationClass()));
    }
}
