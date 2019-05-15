package testaccessors.internal.base

import androidx.annotation.RestrictTo
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

import android.support.annotation.RestrictTo as SupportRestrictTo

abstract class AnnotationProcessor(val annotationClass: Class<out Annotation>)
  : AbstractProcessor(), Options {
  lateinit var filer: Filer
  lateinit var elementUtils: Elements
  lateinit var typeUtils: Types
  lateinit var messager: Messager
  lateinit var options: Map<String, String>

  @Synchronized
  override fun init(processingEnvironment: ProcessingEnvironment) {
    super.init(processingEnvironment)
    filer = processingEnvironment.filer
    elementUtils = processingEnvironment.elementUtils
    typeUtils = processingEnvironment.typeUtils
    messager = processingEnvironment.messager
    options = processingEnvironment.options
  }

  override fun getSupportedAnnotationTypes() = setOf(annotationClass.name)

  override fun getSupportedOptions() = setOf(
      OPTION_KEY_REQUIRED_PATTERN_IN_CLASSPATH,
      OPTION_KEY_ANDROIDX_RESTRICT_TO,
      OPTION_KEY_SUPPORT_RESTRICT_TO)

  override fun getSupportedSourceVersion() = SourceVersion.latest()!!

  override fun requiredPatternInClasspath() =
    options.getOrDefault(
        OPTION_KEY_REQUIRED_PATTERN_IN_CLASSPATH,
        OPTION_DEFAULT_REQUIRED_PATTERN_IN_CLASSPATH).let {
      try {
        Pattern.compile(it)
        it
      } catch (ignored: PatternSyntaxException) {
        OPTION_DEFAULT_REQUIRED_PATTERN_IN_CLASSPATH
      }
  }

  override fun defaultAndroidXRestrictTo(): Iterable<RestrictTo.Scope> {
    val optionToCandidate = { it: String ->
      it.split(",".toRegex())
          .dropLastWhile { it.isEmpty() }
          .toTypedArray()
    }

    return try {
      optionToCandidate(options.getOrDefault(
          OPTION_KEY_ANDROIDX_RESTRICT_TO, OPTION_DEFAULT_ANDROIDX_RESTRICT_TO))
          .map { RestrictTo.Scope.valueOf(it) }
    } catch (ignored: IllegalArgumentException) {
      optionToCandidate(OPTION_DEFAULT_ANDROIDX_RESTRICT_TO).map { RestrictTo.Scope.valueOf(it) }
    }
  }

  override fun defaultSupportRestrictTo(): Iterable<SupportRestrictTo.Scope> {
    val optionToCandidate = { it: String ->
      it.split(",".toRegex())
          .dropLastWhile { it.isEmpty() }
          .toTypedArray()
    }

    return try {
      optionToCandidate(options.getOrDefault(
          OPTION_KEY_SUPPORT_RESTRICT_TO, OPTION_DEFAULT_SUPPORT_RESTRICT_TO))
          .map { SupportRestrictTo.Scope.valueOf(it) }
    } catch (ignored: IllegalArgumentException) {
      optionToCandidate(OPTION_DEFAULT_SUPPORT_RESTRICT_TO)
          .map { SupportRestrictTo.Scope.valueOf(it) }
    }
  }

  private companion object {
    const val OPTION_KEY_REQUIRED_PATTERN_IN_CLASSPATH = "testaccessors.requiredPatternInClasspath"
    const val OPTION_DEFAULT_REQUIRED_PATTERN_IN_CLASSPATH = "junit|testng"
    const val OPTION_KEY_ANDROIDX_RESTRICT_TO = "testaccessors.defaultAndroidXRestrictTo"
    const val OPTION_DEFAULT_ANDROIDX_RESTRICT_TO = ""
    const val OPTION_KEY_SUPPORT_RESTRICT_TO = "testaccessors.defaultSupportRestrictTo"
    const val OPTION_DEFAULT_SUPPORT_RESTRICT_TO = ""
  }
}
