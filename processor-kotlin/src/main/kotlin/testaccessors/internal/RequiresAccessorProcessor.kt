package testaccessors.internal

import com.squareup.kotlinpoet.ClassName
import me.eugeniomarletti.kotlin.metadata.KotlinMetadataUtils
import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import testaccessors.RequiresAccessor
import java.util.HashMap
import java.util.function.Supplier
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

class RequiresAccessorProcessor : KotlinAbstractProcessor(), KotlinMetadataUtils {
  private val logger = Lazy<Logger>(Supplier<Logger> { Logger(messager) })
  private val verifier by lazy { RequiresAccessorAnnotationVerifier(logger) }
  private val writer by lazy { AccessorWriter(elementUtils, typeUtils, logger, delegate) }
  private val filesToGenerate = HashMap<ClassName, MutableSet<Element>>()

  inner class DelegateProcessor : AnnotationProcessor(RequiresAccessor::class.java) {
    override fun process(typeElements: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean {
      roundEnvironment.getElementsAnnotatedWith(delegate.annotationClass).stream()
          .filter { verifier.verify(it) }
          .forEach {
            val className = ClassName("", it.enclosingElement.asType().toString())
            filesToGenerate.getOrDefault(className, mutableSetOf()).run {
              add(it)
              filesToGenerate[className] = this
            }
          }
      if (roundEnvironment.processingOver()) {
        filesToGenerate.values
            .parallelStream()
            .forEach { elements -> writer.writeAccessorClass(elements, filer) }
      }
      return true
    }
  }

  private val delegate = DelegateProcessor()

  override fun init(processingEnv: ProcessingEnvironment) = super.init(processingEnv).also {
    delegate.init(processingEnv)
  }

  override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment) =
      delegate.process(annotations, roundEnv)

  override fun getSupportedAnnotationTypes(): Set<String> = delegate.supportedAnnotationTypes

  override fun getSupportedOptions(): Set<String> = delegate.supportedOptions

  override fun getSupportedSourceVersion(): SourceVersion = delegate.supportedSourceVersion
}
