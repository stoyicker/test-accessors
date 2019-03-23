package testaccessors.internal;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Set;

abstract class AbstractAccessorWriter {
    final Types typeUtils;
    final Elements elementUtils;

    AbstractAccessorWriter(Types typeUtils, Elements elementUtils) {
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
    }

    final String nameForGeneratedClassFrom(final List<String> enclosingClassSimpleNames) {
        return enclosingClassSimpleNames.get(enclosingClassSimpleNames.size() - 1) + "TestAccessors";
    }

    final RuntimeException illegalAccessorRequestedException(final Element element) {
        return new IllegalStateException("Illegal accessor type requested " + element.getSimpleName());
    }

    abstract void writeAccessorClass(final Set<Element> annotatedElements, final Filer filer);

    static final String PARAMETER_NAME_NEW_VALUE = "newValue";
}
