package testaccessors.internal;

import com.squareup.javapoet.ClassName;
import testaccessors.RequiresAccessor;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class RequiresAccessorProcessor extends AnnotationProcessor {
    private final AnnotationVerifier verifier = new RequiresAccessorAnnotationVerifier(messager);
    private final Lazy<AbstractAccessorWriter> writer =
            new Lazy<>(() -> new AccessorWriter(elementUtils, optionRequiredPatternInClasspath()));
    private final Map<ClassName, Set<Element>> filesToGenerate = new HashMap<>();

    public RequiresAccessorProcessor() {
        super(RequiresAccessor.class);
    }

    @Override
    public boolean process(final Set<? extends TypeElement> typeElements, final RoundEnvironment roundEnvironment) {
        roundEnvironment.getElementsAnnotatedWith(getAnnotationClass()).stream()
                .filter(verifier::verify)
                .forEach((Consumer<Element>) element -> {
                    final ClassName className = ClassName.get(
                            "", element.getEnclosingElement().asType().toString());
                    final Set<Element> set = filesToGenerate.getOrDefault(className, new HashSet<>());
                    set.add(element);
                    filesToGenerate.put(className, set);
                });
        if (roundEnvironment.processingOver()) {
            filesToGenerate.values()
                    .parallelStream()
                    .forEach(elements -> writer.getOrCompute().writeAccessorClass(elements, filer));
        }
        return true;
    }
}
