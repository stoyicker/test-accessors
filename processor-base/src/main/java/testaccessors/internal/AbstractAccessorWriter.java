package testaccessors.internal;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.util.Elements;
import java.util.List;
import java.util.Set;

abstract class AbstractAccessorWriter {
    final Elements elementUtils;

    AbstractAccessorWriter(final Elements elementUtils) {
        this.elementUtils = elementUtils;
    }

    final String nameForGeneratedClassFrom(final List<String> enclosingClassSimpleNames) {
        return String.join("", enclosingClassSimpleNames) + "TestAccessors";
    }

    final String[] extractLocation(final Element element) {
        final Element enclosingElement = element.getEnclosingElement();
        final String[] added = enclosingElement == null ? new String[] {} : extractLocation(enclosingElement);
        final String[] current = new String[] { element instanceof PackageElement ? ((PackageElement) element).getQualifiedName().toString() : element.getSimpleName().toString() };
        final String[] ret = new String[added.length + current.length];
        System.arraycopy(added, 0, ret, 0, added.length);
        System.arraycopy(current, 0, ret, added.length, current.length);
        return ret;
    }

    final RuntimeException illegalAccessorRequestedException(final Element element) {
        return new IllegalStateException("Illegal accessor type requested " + element.getSimpleName());
    }

    abstract void writeAccessorClass(final Set<Element> annotatedElements, final Filer filer);

    static final String PARAMETER_NAME_NEW_VALUE = "newValue";
}
