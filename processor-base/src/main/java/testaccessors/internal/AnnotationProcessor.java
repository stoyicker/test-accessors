package testaccessors.internal;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import androidx.annotation.RestrictTo;

public abstract class AnnotationProcessor extends AbstractProcessor implements Options {
  private static final String OPTION_KEY_REQUIRED_PATTERN_IN_CLASSPATH = "testaccessors.requiredPatternInClasspath";
  private static final String OPTION_DEFAULT_REQUIRED_PATTERN_IN_CLASSPATH = "junit|testng";
  private static final String OPTION_KEY_ANDROIDX_RESTRICT_TO = "testaccessors.defaultAndroidXRestrictTo";
  private static final String OPTION_DEFAULT_ANDROIDX_RESTRICT_TO = "";
  private static final String OPTION_KEY_SUPPORT_RESTRICT_TO = "testaccessors.defaultSupportRestrictTo";
  private static final String OPTION_DEFAULT_SUPPORT_RESTRICT_TO = "";
  private final Class<? extends Annotation> annotation;
  Filer filer;
  Elements elementUtils;
  Types typeUtils;
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
    typeUtils = processingEnvironment.getTypeUtils();
    messager = processingEnvironment.getMessager();
    options = processingEnvironment.getOptions();
  }

  @Override
  public final Set<String> getSupportedAnnotationTypes() {
    return Collections.singleton(annotation.getName());
  }

  @Override
  public final Set<String> getSupportedOptions() {
    return new HashSet<>(Arrays.asList(
        OPTION_KEY_REQUIRED_PATTERN_IN_CLASSPATH,
        OPTION_KEY_ANDROIDX_RESTRICT_TO,
        OPTION_KEY_SUPPORT_RESTRICT_TO));
  }

  @Override
  public final SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  final Class<? extends Annotation> getAnnotationClass() {
    return annotation;
  }

  @Override
  public final CharSequence requiredPatternInClasspath() {
    String candidate = options.getOrDefault(
        OPTION_KEY_REQUIRED_PATTERN_IN_CLASSPATH, OPTION_DEFAULT_REQUIRED_PATTERN_IN_CLASSPATH);

    try {
      Pattern.compile(candidate);
    } catch (final PatternSyntaxException ignored) {
      candidate = OPTION_DEFAULT_REQUIRED_PATTERN_IN_CLASSPATH;
    }
    return candidate;
  }

  @Override
  public RestrictTo.Scope[] defaultAndroidXRestrictTo() {
    String[] candidate = options.getOrDefault(
        OPTION_KEY_ANDROIDX_RESTRICT_TO, OPTION_DEFAULT_ANDROIDX_RESTRICT_TO)
        .split(",");

    RestrictTo.Scope[] ret = new RestrictTo.Scope[candidate.length];
    try {
      for (int i = 0; i < candidate.length; i++) {
        ret[i] = RestrictTo.Scope.valueOf(candidate[i]);
      }
    } catch (final IllegalArgumentException ignored) {
      candidate = OPTION_DEFAULT_ANDROIDX_RESTRICT_TO.split(",");
      ret = new RestrictTo.Scope[candidate[0].isEmpty() ? 0 : candidate.length];
        for (int i = 0; i < ret.length; i++) {
          ret[i] = RestrictTo.Scope.valueOf(candidate[i]);
      }
    }

    return ret;
  }

  @Override
  public android.support.annotation.RestrictTo.Scope[] defaultSupportRestrictTo() {
    String[] candidate = options.getOrDefault(
        OPTION_KEY_SUPPORT_RESTRICT_TO, OPTION_DEFAULT_SUPPORT_RESTRICT_TO)
        .split(",");

    android.support.annotation.RestrictTo.Scope[] ret =
        new android.support.annotation.RestrictTo.Scope[candidate.length];
    try {
      for (int i = 0; i < candidate.length; i++) {
        ret[i] = android.support.annotation.RestrictTo.Scope.valueOf(candidate[i]);
      }
    } catch (final IllegalArgumentException ignored) {
      candidate = OPTION_DEFAULT_SUPPORT_RESTRICT_TO.split(",");
      ret = new android.support.annotation.RestrictTo.Scope[candidate[0].isEmpty() ? 0 : candidate.length];
        for (int i = 0; i < ret.length; i++) {
          ret[i] = android.support.annotation.RestrictTo.Scope.valueOf(candidate[i]);
      }
    }

    return ret;
  }
}
