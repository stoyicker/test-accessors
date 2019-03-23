package testaccessors.internal

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import testaccessors.RequiresAccessor
import javax.annotation.Generated
import javax.annotation.processing.Filer
import javax.lang.model.element.Element
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeVariable
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.StandardLocation

internal class AccessorWriter(types: Types, elementUtils: Elements) : AbstractAccessorWriter(types, elementUtils) {
	public override fun writeAccessorClass(annotatedElements: Set<Element>, filer: Filer) {
		// TODO Correctly place/name inner classes and static inner classes
		val enclosingClassElement = annotatedElements.iterator().next().enclosingElement
		val enclosingClassPackage = elementUtils.getPackageOf(enclosingClassElement)
		val enclosingClassErased = ClassName(
				enclosingClassPackage.qualifiedName.toString(), enclosingClassElement.simpleName.toString())
		val classAndFileName = nameForGeneratedClassFrom(enclosingClassErased.simpleNames)
		val typeSpecBuilder = TypeSpec.objectBuilder(classAndFileName)
				.addAnnotation(Generated::class)
		annotatedElements.flatMap(object : (Element) -> Iterable<FunSpec> {
			override fun invoke(element: Element) =
					element.getAnnotation(RequiresAccessor::class.java).requires.map {
						when (it) {
							RequiresAccessor.AccessorType.TYPE_GETTER -> generateGetterMethodSpec(element)
							RequiresAccessor.AccessorType.TYPE_SETTER -> generateSetterMethodSpec(element)
							else -> throw illegalAccessorRequestedException(element)
						}
					}

			private fun generateGetterMethodSpec(element: Element) = element.getAnnotation(RequiresAccessor::class.java)
					.run {
						FunSpec.builder(if (name.isEmpty()) element.simpleName.toString() else name)
								.addAnnotation(JvmStatic::class)
								.addTypeVariables((enclosingClassElement.asType() as DeclaredType).typeArguments.map {
									(it as TypeVariable).let {
										TypeVariableName.invoke(it.asElement().simpleName.toString()).apply {
										}
									}
								})
								.receiver(enclosingClassElement.asType().asTypeName())
								.addStatement("return " + element.simpleName)
								.build()
					}

			private fun generateSetterMethodSpec(element: Element) = element.getAnnotation(RequiresAccessor::class.java)
					.run {
						FunSpec.builder(if (name.isEmpty()) element.simpleName.toString() else name)
								.addAnnotation(JvmStatic::class)
								.addTypeVariables((enclosingClassElement.asType() as DeclaredType).typeArguments.map {
									(it as TypeVariable).let {
										TypeVariableName.invoke(it.asElement().simpleName.toString()).apply {
										}
									}
								})
								.receiver(enclosingClassElement.asType().asTypeName())
								.addParameter(ParameterSpec.builder(
										PARAMETER_NAME_NEW_VALUE,
										element.asType().asTypeName())
										.build())
								.addStatement("${element.simpleName} = $PARAMETER_NAME_NEW_VALUE")
								.build()
					}
		}).forEach { typeSpecBuilder.addFunction(it) }
		FileSpec.builder(enclosingClassPackage.simpleName.toString(), classAndFileName)
				.addAnnotation(AnnotationSpec.builder(JvmName::class)
						.addMember(""""$classAndFileName"""")
						.useSiteTarget(AnnotationSpec.UseSiteTarget.FILE)
						.build())
				.addType(typeSpecBuilder.build())
				.indent("  ")
				.build()
				.writeTo(filer)
	}

	// FIXME KotlinPoet will hopefully add support for Filer at some point in the future
	private fun FileSpec.writeTo(filer: Filer) {
		val fileObject = filer.createResource(
				StandardLocation.SOURCE_OUTPUT, packageName, "$name.kt")
		try {
			fileObject.openWriter().use {
				writeTo(it)
			}
		} catch (e: Exception) {
			fileObject.delete()
			throw e
		}
	}
}
