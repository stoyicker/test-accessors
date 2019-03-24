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
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.type.DeclaredType
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.StandardLocation

internal class AccessorWriter(types: Types, elementUtils: Elements) : AbstractAccessorWriter(types, elementUtils) {
	public override fun writeAccessorClass(annotatedElements: Set<Element>, filer: Filer) {
		val enclosingClassElement = annotatedElements.iterator().next().enclosingElement
		val enclosingClassPackage = elementUtils.getPackageOf(enclosingClassElement)
		val classNames = extractParentClasses(enclosingClassElement.enclosingElement).reversedArray() +
				enclosingClassElement.simpleName.toString()
		val enclosingClassErased = ClassName(
				enclosingClassPackage.qualifiedName.toString(),
				classNames.first(),
				*classNames.sliceArray(1..classNames.lastIndex))
		val classAndFileName = nameForGeneratedClassFrom(enclosingClassErased.simpleNames)
		val typeSpecBuilder = TypeSpec.objectBuilder(classAndFileName)
				.addAnnotation(Generated::class)
		annotatedElements.flatMap(object : (Element) -> Iterable<FunSpec> {
			override fun invoke(element: Element) =
					element.getAnnotation(RequiresAccessor::class.java).requires.map {
						when (it) {
							RequiresAccessor.AccessorType.TYPE_GETTER -> generateGetterFunSpec(element)
							RequiresAccessor.AccessorType.TYPE_SETTER -> generateSetterFunSpec(element)
							else -> throw illegalAccessorRequestedException(element)
						}
					}

			private fun generateGetterFunSpec(element: Element) = generateCommonFunSpec(element)
					.addStatement("return " + element.simpleName)
					.build()

			private fun generateSetterFunSpec(element: Element) = generateCommonFunSpec(element)
					.addParameter(ParameterSpec.builder(
							PARAMETER_NAME_NEW_VALUE,
							element.asType().asTypeName())
							.build())
					.addStatement("${element.simpleName} = $PARAMETER_NAME_NEW_VALUE")
					.build()

			private fun generateCommonFunSpec(element: Element) = element.getAnnotation(RequiresAccessor::class.java)
					.run {
						val typeVariables = mutableSetOf<TypeVariableName>()
						typeVariables += (enclosingClassElement.asType() as DeclaredType).typeArguments.map {
							TypeVariableName(it.asTypeName().toString())
						}
						var enclosingElement = enclosingClassElement.enclosingElement
						while (enclosingElement != null &&
								!enclosingElement.modifiers.contains(Modifier.STATIC) &&
								enclosingElement.kind != ElementKind.PACKAGE) {
							typeVariables += (enclosingElement.asType() as DeclaredType).typeArguments.map {
								TypeVariableName(it.asTypeName().toString())
							}
							enclosingElement = enclosingElement.enclosingElement
						}
						FunSpec.builder(if (name.isEmpty()) element.simpleName.toString() else name)
								.addAnnotation(JvmStatic::class)
								.addTypeVariables(typeVariables)
								.receiver(enclosingClassElement.asType().asTypeName())
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

	private fun isTopLevel(element: Element?): Boolean = element?.run {
		when (enclosingElement) {
			null -> true
			else -> enclosingElement.kind == ElementKind.PACKAGE
		}
	} ?: true

	private fun extractParentClasses(element: Element?): Array<String> = element?.let {
		arrayOf(it.simpleName.toString()) + when (isTopLevel(it.enclosingElement)) {
			true -> if (it.enclosingElement == null) emptyArray() else arrayOf(it.enclosingElement.simpleName.toString())
			false -> extractParentClasses(it.enclosingElement)
		}
	} ?: emptyArray()

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
