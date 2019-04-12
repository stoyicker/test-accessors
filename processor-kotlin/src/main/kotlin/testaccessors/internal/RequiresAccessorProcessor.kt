package testaccessors.internal

import com.squareup.kotlinpoet.ClassName
import testaccessors.RequiresAccessor
import java.util.HashMap
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

class RequiresAccessorProcessor : AnnotationProcessor(RequiresAccessor::class.java) {
	private val verifier by lazy { RequiresAccessorAnnotationVerifier(messager) }
	private val writer by lazy { AccessorWriter(elementUtils, typeUtils, this) }
	private val filesToGenerate = HashMap<ClassName, MutableSet<Element>>()

	override fun process(typeElements: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean {
		roundEnvironment.getElementsAnnotatedWith(annotationClass).stream()
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
