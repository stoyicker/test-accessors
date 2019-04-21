package testaccessors.internal

import androidx.annotation.RestrictTo
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import testaccessors.RequiresAccessor
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.regex.Pattern
import javax.annotation.processing.Filer
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import kotlin.reflect.KClass
import android.support.annotation.RestrictTo as SupportRestrictTo

internal class AccessorWriter(
    private val outputDir: Path,
    elementUtils: Elements,
    typeUtils: Types,
    logger: Lazy<Logger>,
    options: Options)
  : AbstractAccessorWriter(elementUtils, typeUtils, logger, options) {
  public override fun writeAccessorClass(annotatedElements: Set<Element>, filer: Filer) {
    val enclosingClassElement = annotatedElements.iterator().next().enclosingElement
    val location = extractLocation(enclosingClassElement.enclosingElement) +
        enclosingClassElement.simpleName.toString()
    val fileName = nameForGeneratedClassFrom(
        ClassName(location[0], location[1], *location.sliceArray(2..location.lastIndex)).simpleNames)
    val fileSpecBuilder = FileSpec.builder(
        elementUtils.getPackageOf(enclosingClassElement).qualifiedName.toString(), fileName)
        .addAnnotation(AnnotationSpec.builder(JvmName::class)
            .addMember(""""$fileName"""")
            .useSiteTarget(AnnotationSpec.UseSiteTarget.FILE)
            .build())
        .indent("  ")
    annotatedElements.flatMap(object : (Element) -> Iterable<FunSpec> {
      override fun invoke(element: Element) =
          element.getAnnotation(RequiresAccessor::class.java).requires.map {
            val modifiers = element.modifiers
            val isStatic = Modifier.STATIC in modifiers
            when (it) {
              RequiresAccessor.AccessorType.TYPE_GETTER -> if (isStatic)
                generateStaticGetterFunSpec(element) else generateGetterFunSpec(element)
              RequiresAccessor.AccessorType.TYPE_SETTER -> {
                if (isStatic && Modifier.FINAL in modifiers) {
                  logger.orCompute.error(ERROR_MESSAGE_UNSUPPORTED_STATIC_FINAL_SETTER, element)
                }
                if (isStatic)
                  generateStaticSetterFunSpec(element) else generateSetterFunSpec(element)
              }
            }
          }

      private fun generateStaticGetterFunSpec(element: Element) = element.enclosingElement.kotlinize().run {
        generateCommonGetterFunSpec(
            element, "/kdoc-getter-static.template",
            arrayOf(
                toString().replace(Regex("<(.*?)>"), "").replace("<>", ""),
                element.simpleName.toString()),
            "null")
            .receiver(ClassName.bestGuess(KClass::class.qualifiedName!!).plusParameter(this))
            .build()
      }

      private fun generateGetterFunSpec(element: Element) = element.kotlinize().run {
        generateCommonGetterFunSpec(
            element, "/kdoc-getter.template",
            arrayOf(
                element.enclosingElement.kotlinize().toString().replace(Regex("<(.*?)>"), "").replace("<>", ""),
                element.simpleName.toString()),
            "this@${funName(element)}")
            .addReceiver(element)
            .build()
      }

      private fun generateCommonGetterFunSpec(
          element: Element,
          kdocResource: String,
          kdocArgs: Array<Any>,
          receiverLiteral: Any) = element.kotlinize().run {
        generateCommonFunSpec(element)
            .addKdoc(
                javaClass.getResource(kdocResource).readText(StandardCharsets.UTF_8), *kdocArgs)
            .beginControlFlow(
                "%T::class.java.getDeclaredField(%S).apply",
                typeUtils.erasure(element.enclosingElement.asType()),
                element.simpleName)
            .addStatement("val wasAccessible = isAccessible")
            .addStatement("isAccessible = true")
            .addStatement("val ret = this[%L] as %T", receiverLiteral, this)
            .addStatement("isAccessible = wasAccessible")
            .addStatement("return ret")
            .endControlFlow()
            .returns(this)
      }

      private fun generateStaticSetterFunSpec(element: Element) = element.enclosingElement.kotlinize().run {
        generateCommonSetterFunSpec(
            element,
            "/kdoc-setter-static.template",
            arrayOf(
                toString().replace(Regex("<(.*?)>"), "").replace("<>", ""),
                element.simpleName.toString(),
                PARAMETER_NAME_NEW_VALUE),
            "null")
            .receiver(ClassName.bestGuess(KClass::class.qualifiedName!!).plusParameter(this))
            .build()
      }

      private fun generateSetterFunSpec(element: Element) = generateCommonSetterFunSpec(
          element,
          "/kdoc-setter.template",
          arrayOf(
              element.enclosingElement.kotlinize().toString().replace(Regex("<(.*?)>"), "").replace("<>", ""),
              element.simpleName.toString(),
              PARAMETER_NAME_NEW_VALUE),
          "this@${funName(element)}")
          .addReceiver(element)
          .build()

      private fun generateCommonSetterFunSpec(
          element: Element,
          kdocResource: String,
          kdocArgs: Array<Any>,
          receiverLiteral: Any) =
          generateCommonFunSpec(element)
              .addParameter(ParameterSpec.builder(
                  PARAMETER_NAME_NEW_VALUE,
                  element.kotlinize())
                  .build())
              .addKdoc(
                  javaClass.getResource(kdocResource).readText(StandardCharsets.UTF_8), *kdocArgs)
              .beginControlFlow(
                  "%T::class.java.getDeclaredField(%S).apply",
                  typeUtils.erasure(element.enclosingElement.asType()),
                  element.simpleName)
              .addStatement("val wasAccessible = isAccessible")
              .addStatement("isAccessible = true")
              .addStatement("set(%L, %L)", receiverLiteral, PARAMETER_NAME_NEW_VALUE)
              .addStatement("isAccessible = wasAccessible")
              .endControlFlow()

      private fun generateCommonFunSpec(element: Element) = element.getAnnotation(RequiresAccessor::class.java)
          .run {
            FunSpec.builder(funName(element))
                .addAnnotation(
                    AnnotationSpec.builder(Suppress::class)
                        .addMember("%S, %S", "UNCHECKED_CAST", "REDUNDANT_PROJECTION")
                        .build())
                .addAndroidXRestrictTo(androidXRestrictTo)
                .addSupportRestrictTo(supportRestrictTo)
                .apply {
                  val requiredPatternInClasspath = options.requiredPatternInClasspath()
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

      private fun funName(element: Element) = element.getAnnotation(RequiresAccessor::class.java)
          .run {
            if (isName(name)) name else element.simpleName.toString()
          }

      private fun FunSpec.Builder.addAndroidXRestrictTo(annotation: RestrictTo) =
          annotation.value.let {
            val prefix = "${RestrictTo::class.simpleName}.${RestrictTo.Scope::class.simpleName}."
            addAnnotation(
                RestrictTo::class,
                "value",
                (if (it.isEmpty())
                  options.defaultAndroidXRestrictTo()
                else
                  it).map { "$prefix${it.name}" })
            this
          }

      private fun FunSpec.Builder.addSupportRestrictTo(
          annotation: SupportRestrictTo) =
          annotation.value.let {
            val prefix = "${SupportRestrictTo::class.simpleName}.${SupportRestrictTo.Scope::class.simpleName}"
            addAnnotation(
                SupportRestrictTo::class,
                "value",
                (if (it.isEmpty())
                  options.defaultSupportRestrictTo()
                else
                  it).map { "$prefix${it.name}" })
            this
          }

      private fun FunSpec.Builder.addAnnotation(
          annotationClass: KClass<out Annotation>,
          key: String,
          value: Collection<String>) = apply {
        value.takeIf { !it.isNullOrEmpty() }?.let {
          addAnnotation(AnnotationSpec.builder(annotationClass)
              .addMember(CodeBlock.builder()
                  .add("$key = [")
                  .add(it.joinToString(", "))
                  .add("]")
                  .build())
              .build())
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
            .map { it.kotlinize() }
            .filter { it is ParameterizedTypeName }
            .flatMap { (it as ParameterizedTypeName).typeArguments }
            .distinct()
            .map {
              TypeVariableName(it.toString(), variance = (it as TypeVariableName).variance).copy(
                  nullable = it.isNullable,
                  bounds = it.bounds.map { bound -> bound.kotlinize() })
            })
      }
    }).forEach { fileSpecBuilder.addFunction(it) }
    fileSpecBuilder
        .build()
        .writeTo(outputDir)
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
