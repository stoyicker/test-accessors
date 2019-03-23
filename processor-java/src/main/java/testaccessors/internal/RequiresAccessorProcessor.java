package testaccessors.internal;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import testaccessors.RequiresAccessor;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class RequiresAccessorProcessor extends AnnotationProcessor {
    private final AnnotationVerifier verifier = new RequiresAccessorAnnotationVerifier(messager);
    private final AbstractAccessorWriter writer = new AccessorWriter();
    private final Map<ClassName, Set<Element>> filesToGenerate = new HashMap<>();

    RequiresAccessorProcessor() {
        super(RequiresAccessor.class);
    }

    @Override
    public boolean process(final Set<? extends TypeElement> set, final RoundEnvironment roundEnvironment) {
        roundEnvironment.getElementsAnnotatedWith(getAnnotationClass()).stream()
                .filter(verifier::verify)
                .forEach((Consumer<Element>) element -> {
                    final ClassName className = (ClassName) TypeName.get(element.getEnclosingElement().asType());
                    final Set<Element> set1 = filesToGenerate.getOrDefault(className, Collections.emptySet());
                    set1.add(element);
                    filesToGenerate.put(className, set1);
                });
        if (roundEnvironment.processingOver()) {
            filesToGenerate.values().parallelStream()
                    .forEach(elements -> writer.writeAccessorClass(elements, filer));
        }
        return true;
    }
}
