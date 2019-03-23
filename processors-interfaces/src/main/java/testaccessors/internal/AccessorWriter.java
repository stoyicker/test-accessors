package testaccessors.internal;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import java.util.Set;

final class AccessorWriter extends AbstractAccessorWriter {
    AccessorWriter(Messager messager) {
        // NOOP, this is only intended for compile-time safety only
        throw new IllegalStateException("This code should never run under a normal situation. Use one of the language-specific artifacts instead");
    }

    @Override
    void writeAccessorClass(final Set<Element> annotatedElements, final Filer filer) {
        // NOOP, this is only intended for compile-time safety only
        throw new IllegalStateException("This code should never run under a normal situation. Use one of the language-specific artifacts instead");
    }
}
