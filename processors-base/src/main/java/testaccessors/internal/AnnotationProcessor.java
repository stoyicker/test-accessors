package testaccessors.internal;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public abstract class AnnotationProcessor extends AbstractProcessor {
    Filer filer;
    Messager messager;
    private Map<String, String> options;
    private final Class<? extends Annotation> annotation;

    AnnotationProcessor(final Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    @Override
    public synchronized final void init(final ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        options = processingEnvironment.getOptions();
    }

    @Override
    public final Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(annotation.getName());
    }

    @Override
    public final Set<String> getSupportedOptions() {
        return Collections.emptySet();
    }

    @Override
    public final SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    final Class<? extends Annotation> getAnnotationClass() {
        return annotation;
    }
}
