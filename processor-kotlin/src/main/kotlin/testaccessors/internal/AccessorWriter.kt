package testaccessors.internal

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import testaccessors.RequiresAccessor
import javax.annotation.processing.Filer
import javax.lang.model.element.Element
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

internal class AccessorWriter(types: Types, elementUtils: Elements) : AbstractAccessorWriter(types, elementUtils) {
	public override fun writeAccessorClass(annotatedElements: Set<Element>, filer: Filer) {
		val enclosingClassElement = annotatedElements.iterator().next().enclosingElement
		val enclosingClassPackage = elementUtils.getPackageOf(enclosingClassElement)
		val enclosingClassType = enclosingClassElement.asType()
		val enclosingClassRawType = typeUtils.erasure(enclosingClassType)
		val enclosingClass = ClassName(enclosingClassPackage.qualifiedName.toString(), enclosingClassRawType.toString())
		val classAndFileName = nameForGeneratedClassFrom(enclosingClass.simpleNames)
		val typeSpecBuilder = TypeSpec.objectBuilder(classAndFileName)
				.addAnnotation(JvmStatic::class)
				.addModifiers(KModifier.PUBLIC, KModifier.FINAL)
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
								.addModifiers(KModifier.PUBLIC)
								.receiver(enclosingClass)
								.addStatement("return " + element.simpleName)
								.returns(ClassName("", element.asType().toString()))
								.build()
					}

			private fun generateSetterMethodSpec(element: Element) = element.getAnnotation(RequiresAccessor::class.java)
					.run {
						FunSpec.builder(if (name.isEmpty()) element.simpleName.toString() else name)
								.addModifiers(KModifier.PUBLIC)
								.receiver(enclosingClass)
								.addParameter(ParameterSpec.builder(
										PARAMETER_NAME_NEW_VALUE, ClassName("", element.asType().toString()))
										.build())
								.addStatement("${element.simpleName} = $PARAMETER_NAME_NEW_VALUE")
								.returns(Void.TYPE)
								.build()
					}
		}).forEach { typeSpecBuilder.addFunction(it) }
		FileSpec.builder(enclosingClass.packageName, classAndFileName)
				.addAnnotation(AnnotationSpec.builder(JvmName::class)
						.addMember("name", classAndFileName)
						.useSiteTarget(AnnotationSpec.UseSiteTarget.FILE)
						.build())
				.addType(typeSpecBuilder.build())
				.build()
				.writeTo(filer)
	}

	private fun FileSpec.writeTo(filer: Filer) {
		val fileName = if (packageName.isEmpty()) name else "$packageName.$name"
		val fileObject = filer.createSourceFile(fileName)
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
