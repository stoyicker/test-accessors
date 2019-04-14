package testaccessors.internal;

import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import testaccessors.RequiresAccessor;

final class RequiresAccessorAnnotationVerifier extends AnnotationVerifier<RequiresAccessor> {
  RequiresAccessorAnnotationVerifier(final Lazy<Logger> logger) {
    super(logger);
  }

  @Override
  Class<RequiresAccessor> annotationClass() {
    return RequiresAccessor.class;
  }

  @Override
  boolean verify(final Element element, final RequiresAccessor annotation) {
    final Set<Modifier> modifierSet = element.getModifiers();
    if (modifierSet.contains(Modifier.PUBLIC) && !modifierSet.contains(Modifier.FINAL)) {
      logger.getOrCompute().warn("Fields that are public and non-final are guaranteed to be accessible from anywhere at anytime, so you don't want to generate accessors for them.", element);
      return false;
    }
    return true;
  }
}
