package testaccessors.internal;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import java.util.List;
import java.util.Set;

abstract class AbstractAccessorWriter {
    final String nameForGeneratedClassFrom(final List<String> enclosingClassSimpleNames) {
        return String.join("$", enclosingClassSimpleNames) + "TestAccessors";
    }

    final RuntimeException illegalAccessorRequestedException(final Element element) {
        return new IllegalStateException("Illegal accessor type requested " + element.getSimpleName());
    }

    abstract void writeAccessorClass(final Set<Element> annotatedElements, final Filer filer);

    static final String PARAMETER_NAME_NEW_VALUE = "newValue";
}
