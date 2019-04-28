package testaccessors.internal

import com.squareup.javapoet.ClassName
import testaccessors.RequiresAccessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

class RequiresAccessorProcessor : AnnotationProcessor(RequiresAccessor::class.java) {
  private val logger by lazy { Logger(messager) }
  private val verifier by lazy { RequiresAccessorAnnotationVerifier(logger) }
  private val writer by lazy { AccessorWriter(elementUtils, typeUtils, logger, this) }
  private val filesToGenerate = mutableMapOf<ClassName, MutableSet<Element>>()

  override fun process(typeElements: Set<TypeElement>, roundEnvironment: RoundEnvironment) = true
      .also {
        roundEnvironment.getElementsAnnotatedWith(annotationClass)
            .filter { verifier.verify(it) }
            .onEach { element ->
              ClassName.get("", element.enclosingElement.asType().toString()).let {
                (filesToGenerate).getOrDefault(it, mutableSetOf()).apply {
                  add(element)
                  filesToGenerate[it] = this
                }
              }
            }
        if (roundEnvironment.processingOver()) {
          filesToGenerate.values.forEach { writer.writeAccessorClass(it, filer) }
        }
      }
}
