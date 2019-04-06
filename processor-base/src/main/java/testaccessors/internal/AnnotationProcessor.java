package testaccessors.internal;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public abstract class AnnotationProcessor extends AbstractProcessor {
  private static final String OPTION_KEY_REQUIRED_PATTERN_IN_CLASSPATH = "testaccessors.requiredPatternInClasspath";
  private static final String OPTION_DEFAULT_REQUIRED_PATTERN_IN_CLASSPATH = "junit|testng";
  private final Class<? extends Annotation> annotation;
  Filer filer;
  Elements elementUtils;
  Messager messager;
  private Map<String, String> options;

  AnnotationProcessor(final Class<? extends Annotation> annotation) {
    this.annotation = annotation;
  }

  @Override
  public synchronized final void init(final ProcessingEnvironment processingEnvironment) {
    super.init(processingEnvironment);
    filer = processingEnvironment.getFiler();
    elementUtils = processingEnvironment.getElementUtils();
    messager = processingEnvironment.getMessager();
    options = processingEnvironment.getOptions();
  }

  @Override
  public final Set<String> getSupportedAnnotationTypes() {
    return Collections.singleton(annotation.getName());
  }

  @Override
  public final Set<String> getSupportedOptions() {
    return Collections.singleton(OPTION_KEY_REQUIRED_PATTERN_IN_CLASSPATH);
  }

  @Override
  public final SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  final Class<? extends Annotation> getAnnotationClass() {
    return annotation;
  }

  final CharSequence optionRequiredPatternInClasspath() {
    String candidate = options.getOrDefault(
        OPTION_KEY_REQUIRED_PATTERN_IN_CLASSPATH, OPTION_DEFAULT_REQUIRED_PATTERN_IN_CLASSPATH);

    try {
      Pattern.compile(candidate);
    } catch (final PatternSyntaxException ignored) {
      candidate = OPTION_DEFAULT_REQUIRED_PATTERN_IN_CLASSPATH;
    }
    return candidate;
  }
}
