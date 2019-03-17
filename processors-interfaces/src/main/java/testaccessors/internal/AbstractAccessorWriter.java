package testaccessors.internal;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import java.util.List;
import java.util.Set;

abstract class AbstractAccessorWriter {
    final String nameForGeneratedClassFrom(final List<String> enclosingClassSimpleNames) {
        return String.join("$", enclosingClassSimpleNames) + "TestAccessors";
    }

    final String setterParameterName() {
        return "newValue";
    }

    abstract void writeAccessorClass(final Set<Element> annotatedElements, final Filer filer);
}
