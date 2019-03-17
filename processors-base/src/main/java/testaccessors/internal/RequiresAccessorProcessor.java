package testaccessors.internal;

import testaccessors.RequiresAccessor;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class RequiresAccessorProcessor extends AnnotationProcessor {
    private final AnnotationVerifier verifier = new RequiresAccessorAnnotationVerifier(messager, optionLogLevel());
    private final AbstractAccessorWriter writer = new AccessorWriter(messager, optionLogLevel());
    private final Map<CharSequence, Set<Element>> filesToGenerate = new HashMap<>();

    RequiresAccessorProcessor() {
        super(RequiresAccessor.class);
    }

    @Override
    public boolean process(final Set<? extends TypeElement> set, final RoundEnvironment roundEnvironment) {
        roundEnvironment.getElementsAnnotatedWith(getAnnotationClass()).stream()
                .filter(verifier::verify)
                .forEach(this::addElement);
        if (roundEnvironment.processingOver()) {
            filesToGenerate.values().parallelStream()
                    .forEach(elements -> writer.writeAccessorClass(elements, filer));
        }
        return true;
    }

    private void addElement(final Element element) {
        final CharSequence className = element.getEnclosingElement().getSimpleName();
        final Set<Element> set = filesToGenerate.getOrDefault(className, Collections.emptySet());
        set.add(element);
        filesToGenerate.put(className, set);
    }
}
