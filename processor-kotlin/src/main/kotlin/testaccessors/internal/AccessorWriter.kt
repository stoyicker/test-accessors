package testaccessors.internal

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import testaccessors.RequiresAccessor
import java.util.regex.Pattern
import javax.annotation.Generated
import javax.annotation.processing.Filer
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.util.Elements

internal class AccessorWriter(elementUtils: Elements, requiredPatternInClasspath: CharSequence?)
	: AbstractAccessorWriter(elementUtils, requiredPatternInClasspath) {
	public override fun writeAccessorClass(annotatedElements: Set<Element>, filer: Filer) {
		val enclosingClassElement = annotatedElements.iterator().next().enclosingElement
		val location = extractLocation(enclosingClassElement.enclosingElement) +
				enclosingClassElement.simpleName.toString()
		val classAndFileName = nameForGeneratedClassFrom(
				ClassName(location[0], location[1], *location.sliceArray(2..location.lastIndex)).simpleNames)
		val typeSpecBuilder = TypeSpec.objectBuilder(classAndFileName)
		annotatedElements.flatMap(object : (Element) -> Iterable<FunSpec> {
			override fun invoke(element: Element) =
					element.getAnnotation(RequiresAccessor::class.java).requires.map {
						when (it) {
							RequiresAccessor.AccessorType.TYPE_GETTER -> generateGetterFunSpec(element)
							RequiresAccessor.AccessorType.TYPE_SETTER -> generateSetterFunSpec(element)
						}
					}

			private fun generateGetterFunSpec(element: Element) = element.asType().asTypeName().kotlinize().run {
				generateCommonFunSpec(element)
						.addStatement("return ${element.simpleName} as %T", this)
						.returns(this)
						.build()
			}

			private fun generateSetterFunSpec(element: Element) = generateCommonFunSpec(element)
					.addParameter(ParameterSpec.builder(
							PARAMETER_NAME_NEW_VALUE,
							element.asType().asTypeName().kotlinize())
							.build())
					.addStatement("${element.simpleName} = $PARAMETER_NAME_NEW_VALUE")
					.build()

			private fun generateCommonFunSpec(element: Element) = element.getAnnotation(RequiresAccessor::class.java)
					.run {
						FunSpec.builder(if (isName(name)) name else element.simpleName.toString())
								.addAnnotation(JvmStatic::class)
								.addReceiver(element)
								.apply {
									if (!requiredPatternInClasspath.isNullOrEmpty()) {
										addCode(CodeBlock.builder()
												.beginControlFlow(
														"if (!%T.compile(%S).matcher(%T.getProperty(%S)).find())",
														Pattern::class,
														requiredPatternInClasspath,
														System::class,
														"java.class.path")
												.addStatement(
														"throw %T(%S)",
														IllegalAccessError::class,
														ERROR_MESSAGE_ILLEGAL_ACCESS)
												.endControlFlow()
												.build())
									}
								}
					}

			private fun FunSpec.Builder.addReceiver(element: Element) = apply {
				receiver(element.enclosingElement.asType().asTypeName())
				val enclosingElementsOf: (Element) -> List<Element> = {
					mutableListOf<Element>().apply {
						var eachEnclosing: Element? = it.enclosingElement
						while (eachEnclosing != null && eachEnclosing.kind != ElementKind.PACKAGE) {
							add(eachEnclosing)
							eachEnclosing = eachEnclosing.enclosingElement
						}
					}
				}
				val enclosingElementList = enclosingElementsOf(element)
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
								nullable = it.isNullable,
								bounds = (it as TypeVariableName).bounds.map { it.kotlinize() })
						})
			}

		}).forEach { typeSpecBuilder.addFunction(it) }
		FileSpec.builder(elementUtils.getPackageOf(enclosingClassElement).qualifiedName.toString(), classAndFileName)
				.addAnnotation(AnnotationSpec.builder(JvmName::class)
						.addMember(""""$classAndFileName"""")
						.useSiteTarget(AnnotationSpec.UseSiteTarget.FILE)
						.build())
				.addAnnotation(AnnotationSpec.builder(Generated::class)
						.useSiteTarget(AnnotationSpec.UseSiteTarget.FILE)
						.addMember(""""${AccessorWriter::class.qualifiedName}"""")
						.build())
				.addType(typeSpecBuilder.build())
				.indent("  ")
				.build()
				.writeTo(filer)
	}
}

private fun isName(name: String?) = !name.isNullOrEmpty() && name.split("\\.").none { it in KEYWORDS }

// https://github.com/JetBrains/kotlin/blob/master/core/descriptors/src/org/jetbrains/kotlin/renderer/KeywordStringsGenerated.java
private val KEYWORDS = setOf(
		"package",
		"as",
		"typealias",
		"class",
		"this",
		"super",
		"val",
		"var",
		"fun",
		"for",
		"null",
		"true",
		"false",
		"is",
		"in",
		"throw",
		"return",
		"break",
		"continue",
		"object",
		"if",
		"try",
		"else",
		"while",
		"do",
		"when",
		"interface",
		"typeof"
)
