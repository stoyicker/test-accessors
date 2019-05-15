package testaccessors.internal

import com.squareup.kotlinpoet.ClassName
import testaccessors.RequiresAccessor
import testaccessors.internal.base.AnnotationProcessor
import testaccessors.internal.base.Logger
import testaccessors.internal.base.RequiresAccessorAnnotationVerifier
import java.nio.file.Paths
import java.util.HashMap
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

class RequiresAccessorProcessor : AnnotationProcessor(RequiresAccessor::class.java) {
	private val logger by lazy { Logger(messager) }
	private val verifier by lazy { RequiresAccessorAnnotationVerifier(logger) }
	private val writer by lazy {
		AccessorWriter(optionKaptKotlinGenerated(), elementUtils, typeUtils, this)
	}
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

	override fun getSupportedOptions() = super.getSupportedOptions().plus(OPTION_KEY_KAPT_KOTLIN_GENERATED)

	private fun optionKaptKotlinGenerated() = Paths.get(options[OPTION_KEY_KAPT_KOTLIN_GENERATED]!!)

	private companion object {
		const val OPTION_KEY_KAPT_KOTLIN_GENERATED = "kapt.kotlin.generated"
	}
}
