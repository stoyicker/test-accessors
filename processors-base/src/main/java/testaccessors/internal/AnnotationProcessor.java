package testaccessors.internal;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public abstract class AnnotationProcessor extends AbstractProcessor {
    Filer filer;
    Messager messager;
    Elements elements;
    Map<String, String> options;
    private final Class<? extends Annotation> annotation;

    AnnotationProcessor(final Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    @Override
    public synchronized final void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        elements = processingEnvironment.getElementUtils();
        options = processingEnvironment.getOptions();
    }

    @Override
    public final Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(annotation.getName());
    }

    @Override
    public final Set<String> getSupportedOptions() {
        final Set<String> ret = new HashSet<>(2);
        ret.add(OPTION_KEY_REQUIRED_PATTERN_IN_CLASSPATH);
        ret.add(OPTION_KEY_LOG_LEVEL);
        return ret;
    }

    @Override
    public final SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    final Class<? extends Annotation> getAnnotationClass() {
        return annotation;
    }

    final CharSequence optionRequiredPatternsInClasspath() {
        String candidate = options.getOrDefault(
                OPTION_KEY_REQUIRED_PATTERN_IN_CLASSPATH, OPTION_DEFAULT_REQUIRED_PATTERN_IN_CLASSPATH);
        try {
            Pattern.compile(candidate);
        } catch (final PatternSyntaxException ignored) {
            candidate = OPTION_DEFAULT_REQUIRED_PATTERN_IN_CLASSPATH;
        }
        return candidate;
    }

    final Logger.LogLevel optionLogLevel() {
        try {
            return Logger.LogLevel.valueOf(options.getOrDefault(OPTION_KEY_LOG_LEVEL, OPTION_DEFAULT_LOG_LEVEL.key));
        } catch (final IllegalArgumentException ignored) {
            return OPTION_DEFAULT_LOG_LEVEL;
        }
    }

    private static final String OPTION_KEY_REQUIRED_PATTERN_IN_CLASSPATH = "testaccessors.requiredPatternInClasspath";
    private static final String OPTION_DEFAULT_REQUIRED_PATTERN_IN_CLASSPATH = "org.junit.*|org.testng.*";
    static final String OPTION_KEY_LOG_LEVEL = "testaccessors.logLevel";
    static final Logger.LogLevel OPTION_DEFAULT_LOG_LEVEL = Logger.LogLevel.LEVEL_NOTE;
}
