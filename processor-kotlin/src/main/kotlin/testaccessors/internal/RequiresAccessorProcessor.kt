package testaccessors.internal

import testaccessors.RequiresAccessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement

class RequiresAccessorProcessor: AnnotationProcessor(RequiresAccessor::class.java) {
	override fun process(set: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean {
		// TODO process kotlin
		return false
	}
}
