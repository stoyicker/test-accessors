package testaccessors.internal

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import testaccessors.RequiresAccessor
import javax.annotation.Generated
import javax.annotation.processing.Filer
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.util.Elements
import javax.tools.StandardLocation

internal class AccessorWriter(elementUtils: Elements) : AbstractAccessorWriter(elementUtils) {
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
// 					FIXME Use this instead when KotlinPoet correctly reports Kotlin types instead of their Java counterparts
//					.addStatement("return ${element.simpleName} as ${element.asType().asTypeName()}")
					.addStatement("return ${element.simpleName}")
					.build()

			private fun generateSetterFunSpec(element: Element) = generateCommonFunSpec(element)
					.addParameter(ParameterSpec.builder(
							PARAMETER_NAME_NEW_VALUE,
							ClassName.bestGuess(Any::class.qualifiedName!!).copy(true))
// 							FIXME Use this instead when KotlinPoet correctly reports Kotlin types instead of their Java counterparts
//							element.asType().asTypeName())
							.build())
					.addStatement("${element.simpleName} = $PARAMETER_NAME_NEW_VALUE")
					.build()

			private fun generateCommonFunSpec(element: Element) = element.getAnnotation(RequiresAccessor::class.java)
					.run {
						FunSpec.builder(if (name.isEmpty()) element.simpleName.toString() else name)
								.addAnnotation(JvmStatic::class)
								.addReceiver(element)
					}

			private fun FunSpec.Builder.addReceiver(element: Element) = apply {
				receiver(element.enclosingElement.asType().asTypeName())
				val enclosingElements: (Element) -> List<Element> = { receiver ->
					mutableListOf<Element>().apply {
						var eachEnclosing: Element? = receiver.enclosingElement
						while (eachEnclosing != null && eachEnclosing.kind != ElementKind.PACKAGE) {
							add(eachEnclosing)
							eachEnclosing = eachEnclosing.enclosingElement
						}
					}
				}
				val enclosingElementList = enclosingElements(element)
				addTypeVariables(enclosingElementList
						.filter {
							enclosingElementList.first() === it ||
									(Modifier.STATIC !in it.modifiers && it.enclosingElement.kind != ElementKind.PACKAGE)
						}
						.map { it.asType().asTypeName() }
						.filter { it is ParameterizedTypeName }
						.flatMap { (it as ParameterizedTypeName).typeArguments }
						.distinct()
						.map {
							TypeVariableName(it.toString()).copy(
//								FIXME Use this instead when KotlinPoet correctly reports Kotlin types instead of their Java counterparts
//								nullable = it.isNullable,
//								bounds = (it as TypeVariableName).bounds)
									nullable = it.isNullable)
						})
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
