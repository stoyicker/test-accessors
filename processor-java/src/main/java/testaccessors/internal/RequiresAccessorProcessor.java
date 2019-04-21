package testaccessors.internal;

import com.squareup.javapoet.ClassName;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import testaccessors.RequiresAccessor;

public final class RequiresAccessorProcessor extends AnnotationProcessor {
  private final Lazy<Logger> logger = new Lazy<>(() -> new Logger(messager));
  private final Lazy<AnnotationVerifier> verifier =
      new Lazy<>(() -> new RequiresAccessorAnnotationVerifier(logger));
  private final Lazy<AbstractAccessorWriter> writer =
      new Lazy<>(() -> new AccessorWriter(elementUtils, typeUtils, logger, this));
  private final Map<ClassName, Set<Element>> filesToGenerate = new HashMap<>();

  public RequiresAccessorProcessor() {
    super(RequiresAccessor.class);
  }

  @Override
  public boolean process(final Set<? extends TypeElement> typeElements, final RoundEnvironment roundEnvironment) {
    roundEnvironment.getElementsAnnotatedWith(getAnnotationClass()).stream()
        .filter((Predicate<Element>) element -> verifier.getOrCompute().verify(element))
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
