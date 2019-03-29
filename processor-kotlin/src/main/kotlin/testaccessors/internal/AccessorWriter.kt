package testaccessors.internal

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import testaccessors.RequiresAccessor
import javax.annotation.Generated
import javax.annotation.processing.Filer
import javax.lang.model.element.Element
import javax.lang.model.element.PackageElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.StandardLocation

// TODO If the enclosing class does not have the static modifier (meaning it is inner) then enclosing classes
// should get wildcard types as well, otherwise things will not work out
internal class AccessorWriter(types: Types, elementUtils: Elements) : AbstractAccessorWriter(types, elementUtils) {
	public override fun writeAccessorClass(annotatedElements: Set<Element>, filer: Filer) {
		val enclosingClassElement = annotatedElements.iterator().next().enclosingElement
		val location = extractLocation(enclosingClassElement.enclosingElement) +
				enclosingClassElement.simpleName.toString()
		val classAndFileName = nameForGeneratedClassFrom(
				ClassName(location[0], location[1], *location.sliceArray(2..location.lastIndex)).simpleNames)
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
					.addTypeVariable(TypeVariableName("T"))
					.addStatement("return " + element.simpleName + " as T")
					.build()

			private fun generateSetterFunSpec(element: Element) = generateCommonFunSpec(element)
					.addParameter(ParameterSpec.builder(
							PARAMETER_NAME_NEW_VALUE,
							ClassName.bestGuess(Any::class.qualifiedName!!).copy(true))
							.build())
					.addStatement("${element.simpleName} = $PARAMETER_NAME_NEW_VALUE")
					.build()

			private fun generateCommonFunSpec(element: Element) = element.getAnnotation(RequiresAccessor::class.java)
					.run {
						FunSpec.builder(if (name.isEmpty()) element.simpleName.toString() else name)
								.addAnnotation(JvmStatic::class)
								.receiver(generateReceiver(element.enclosingElement.asType()))
					}

			private fun generateReceiver(typeMirror: TypeMirror) =
					(typeMirror as DeclaredType).typeArguments.run {
						if (isEmpty()) {
							typeMirror.asTypeName()
						} else {
							ClassName.bestGuess(typeUtils.erasure(typeMirror).toString())
									.parameterizedBy(*map {
										TypeVariableName.invoke("*")
									}.toTypedArray())
						}
					}
		}).forEach { typeSpecBuilder.addFunction(it) }
		FileSpec.builder(elementUtils.getPackageOf(enclosingClassElement).qualifiedName.toString(), classAndFileName)
				.addAnnotation(AnnotationSpec.builder(JvmName::class)
						.addMember(""""$classAndFileName"""")
						.useSiteTarget(AnnotationSpec.UseSiteTarget.FILE)
						.build())
				.addType(typeSpecBuilder.build())
				.indent("  ")
				.build()
				.writeTo(filer)
	}

	private fun extractLocation(element: Element): Array<String> =
			when (element.enclosingElement) {
				null -> emptyArray()
				else -> extractLocation(element.enclosingElement)
			} + arrayOf(if (element is PackageElement) element.qualifiedName.toString() else element.simpleName.toString())
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
