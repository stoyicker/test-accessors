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
    public synchronized final void init(ProcessingEnvironment processingEnvironment) {
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
        return Collections.singleton(OPTION_KEY_LOG_LEVEL);
    }

    @Override
    public final SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    final Class<? extends Annotation> getAnnotationClass() {
        return annotation;
    }

    final LogLevel optionLogLevel() {
        try {
            return LogLevel.valueOf(options.getOrDefault(OPTION_KEY_LOG_LEVEL, OPTION_DEFAULT_LOG_LEVEL.key));
        } catch (final IllegalArgumentException ignored) {
            return OPTION_DEFAULT_LOG_LEVEL;
        }
    }
    static final String OPTION_KEY_LOG_LEVEL = "testaccessors.logLevel";
    private static final LogLevel OPTION_DEFAULT_LOG_LEVEL = LogLevel.LEVEL_NOTE;
}
