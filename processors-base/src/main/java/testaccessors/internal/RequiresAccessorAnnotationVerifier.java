package testaccessors.internal;

import testaccessors.RequiresAccessor;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.util.Set;

final class RequiresAccessorAnnotationVerifier extends AnnotationVerifier<RequiresAccessor> {
    RequiresAccessorAnnotationVerifier(final Messager messager, final LogLevel logLevel) {
        super(messager, logLevel);
    }

    @Override
    Class<RequiresAccessor> annotationClass() {
        return RequiresAccessor.class;
    }

    @Override
    boolean verify(final Element element, final RequiresAccessor annotation) {
        final Set<Modifier> modifierSet = element.getModifiers();
        if (modifierSet.contains(Modifier.PUBLIC) && !modifierSet.contains(Modifier.FINAL)) {
            logger.warn("Fields that are public and non-final are guaranteed to be accessible from anywhere at anytime, so you don't want to generate accessors for them.", element);
            return false;
        }
        if (modifierSet.contains(Modifier.STATIC)) {
            logger.error("Static fields are not member variables and are properties of the Class object instead.\nThese fields cannot be accessed through reflection and therefore this tool cannot generate accessors for them.", element);
            return false;
        }
        return true;
    }
}
