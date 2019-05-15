package testaccessors.internal

import androidx.annotation.RestrictTo
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import testaccessors.RequiresAccessor
import testaccessors.internal.base.AbstractAccessorWriter
import testaccessors.internal.base.Options
import java.lang.reflect.Field
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.regex.Pattern
import javax.annotation.processing.Filer
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import kotlin.reflect.KClass
import android.support.annotation.RestrictTo as SupportRestrictTo

internal class AccessorWriter(
    private val outputDir: Path,
    elementUtils: Elements,
    typeUtils: Types,
    options: Options)
  : AbstractAccessorWriter(elementUtils, typeUtils, options) {
  override fun writeAccessorClass(annotatedElements: Set<Element>, filer: Filer) {
    val enclosingClassElement = annotatedElements.iterator().next().enclosingElement
    val location = extractLocation(enclosingClassElement.enclosingElement) +
        enclosingClassElement.simpleName.toString()
    val fileAndObjectName = nameForGeneratedClassFrom(
        ClassName(location[0], location[1], *location.sliceArray(2..location.lastIndex)).simpleNames)
    val fileSpecBuilder = FileSpec.builder(
        elementUtils.getPackageOf(enclosingClassElement).qualifiedName.toString(), fileAndObjectName)
        .indent("  ")
    val objectSpecBuilder = TypeSpec.objectBuilder(fileAndObjectName)
        .addModifiers(KModifier.INTERNAL)
    annotatedElements.flatMap(object : (Element) -> Iterable<FunSpec> {
      override fun invoke(element: Element) =
          element.getAnnotation(RequiresAccessor::class.java).requires.map {
            val modifiers = element.modifiers
            val isStatic = Modifier.STATIC in modifiers
            when (it) {
              RequiresAccessor.AccessorType.TYPE_GETTER -> if (isStatic)
                generateStaticGetterFunSpec(element) else generateGetterFunSpec(element)
              RequiresAccessor.AccessorType.TYPE_SETTER -> if (isStatic)
                generateStaticSetterFunSpec(element) else generateSetterFunSpec(element)
            }
          }

      private fun generateStaticGetterFunSpec(element: Element) =
          generateCommonGetterFunSpec(
              element,
              "/kdoc-getter-static.template",
              arrayOf(
                  typeUtils.erasure(element.enclosingElement.asType()),
                  element.simpleName.toString()),
              "null")
              .build()

      private fun generateGetterFunSpec(element: Element) =
          generateCommonGetterFunSpec(
              element, "/kdoc-getter.template",
              arrayOf(
                  typeUtils.erasure(element.enclosingElement.asType()),
                  element.simpleName.toString()),
              "this@${funName(element)}")
              .addReceiver(element)
              .build()

      private fun generateCommonGetterFunSpec(
          element: Element,
          kDocResource: String,
          kDocArgs: Array<Any>,
          receiverLiteral: Any) = TypeVariableName(TYPE_NAME_VALUE).run {
        generateCommonFunSpec(element)
            .addAnnotation(
                AnnotationSpec.builder(Suppress::class)
                    .addMember("%S", "UNCHECKED_CAST")
                    .build())
            .addKdoc(
                javaClass.getResource(kDocResource).readText(StandardCharsets.UTF_8), *kDocArgs)
            .beginControlFlow(
                "Class.forName(%S).getDeclaredField(%S).apply",
                element.enclosingElement.toLoadableClassString(),
                element.simpleName)
            .addStatement("val wasAccessible = isAccessible")
            .addStatement("isAccessible = true")
            .addStatement("val ret = this[%L] as %T", receiverLiteral, this)
            .addStatement("isAccessible = wasAccessible")
            .addStatement("return ret")
            .endControlFlow()
            .returns(this)
      }

      private fun generateStaticSetterFunSpec(element: Element) =
          generateCommonFunSpec(element)
              .addParameter(ParameterSpec.builder(
                  PARAMETER_NAME_NEW_VALUE,
                  TypeVariableName(TYPE_NAME_VALUE))
                  .build())
              .addKdoc(
                  javaClass.getResource("/kdoc-setter-static.template").readText(StandardCharsets.UTF_8),
                  TypeVariableName(TYPE_NAME_VALUE),
                  element.simpleName.toString(),
                  PARAMETER_NAME_NEW_VALUE)
              .beginControlFlow(
                  "Class.forName(%S).getDeclaredField(%S).apply",
                  element.enclosingElement.toLoadableClassString(),
                  element.simpleName)
              .addStatement("val wasAccessible = isAccessible")
              .addStatement("isAccessible = true")
              .addStatement("val modifiersField = %T::class.java.getDeclaredField(\"modifiers\")", Field::class)
              .addStatement("val wasModifiersAccessible = modifiersField.isAccessible")
              .addStatement("modifiersField.isAccessible = true")
              .addStatement("modifiersField.setInt(this, modifiers and %T.FINAL.inv())", java.lang.reflect.Modifier::class)
              .addStatement("set(%L, %L)", "null", PARAMETER_NAME_NEW_VALUE)
              .addStatement("modifiersField.isAccessible = wasModifiersAccessible")
              .addStatement("isAccessible = wasAccessible")
              .endControlFlow()
              .build()

      private fun generateSetterFunSpec(element: Element) =
          generateCommonFunSpec(element)
              .addParameter(ParameterSpec.builder(
                  PARAMETER_NAME_NEW_VALUE,
                  TypeVariableName(TYPE_NAME_VALUE))
                  .build())
              .addKdoc(
                  javaClass.getResource("/kdoc-setter.template").readText(StandardCharsets.UTF_8),
                  typeUtils.erasure(element.enclosingElement.asType()),
                  element.simpleName.toString(),
                  PARAMETER_NAME_NEW_VALUE)
              .beginControlFlow(
                  "Class.forName(%S).getDeclaredField(%S).apply",
                  element.enclosingElement.toLoadableClassString(),
                  element.simpleName)
              .addStatement("val wasAccessible = isAccessible")
              .addStatement("isAccessible = true")
              .addStatement("set(%L, %L)", "this@${funName(element)}", PARAMETER_NAME_NEW_VALUE)
              .addStatement("isAccessible = wasAccessible")
              .endControlFlow()
              .addReceiver(element)
              .build()

      private fun generateCommonFunSpec(element: Element) =
          element.getAnnotation(RequiresAccessor::class.java).run {
            FunSpec.builder(funName(element))
                .addAnnotation(JvmStatic::class)
                .addAndroidXRestrictTo(androidXRestrictTo)
                .addSupportRestrictTo(supportRestrictTo)
                .addTypeVariable(TypeVariableName(TYPE_NAME_VALUE))
                .apply {
                  options.requiredPatternInClasspath().let {
                    if (it.isNotEmpty()) {
                      addCode(CodeBlock.builder()
                          .beginControlFlow(
                              "if (!%T.compile(%S).matcher(%T.getProperty(%S)).find())",
                              Pattern::class,
                              it,
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
          }

      private fun funName(element: Element) = element.getAnnotation(RequiresAccessor::class.java)
          .run { if (isName(name)) name else element.simpleName.toString() }

      private fun FunSpec.Builder.addAndroidXRestrictTo(annotation: RestrictTo) = apply {
        val originalScopes = annotation.value.toList()
        val values = if (originalScopes.isEmpty())
          options.defaultAndroidXRestrictTo()
        else
          originalScopes
        val prefix = "${RestrictTo::class.simpleName}.${RestrictTo.Scope::class.simpleName}."
        val valuesAsStrings = values.map { prefix + it.name }
        addAnnotation(RestrictTo::class, "value", valuesAsStrings)
      }

      private fun FunSpec.Builder.addSupportRestrictTo(annotation: SupportRestrictTo) = apply {
        val originalScopes = annotation.value.toList()
        val values = if (originalScopes.isEmpty())
          options.defaultSupportRestrictTo()
        else
          originalScopes
        val prefix = "${SupportRestrictTo::class.simpleName}.${SupportRestrictTo.Scope::class.simpleName}."
        val valuesAsStrings = values.map { prefix + it.name }
        addAnnotation(SupportRestrictTo::class, "value", valuesAsStrings)
      }

      private fun FunSpec.Builder.addAnnotation(
          annotationClass: KClass<out Annotation>,
          key: String,
          values: Iterable<String>?) = apply {
        if (values != null && values.any()) {
          addAnnotation(AnnotationSpec.builder(annotationClass)
              .addMember(CodeBlock.builder()
                  .add("$key = [")
                  .add(values.joinToString(", "))
                  .add("]")
                  .build())
              .build())
        }
      }

      private fun FunSpec.Builder.addReceiver(element: Element) = apply {
        receiver(typeUtils.erasure(element.enclosingElement.asType()).asTypeName())
      }
    }).forEach { objectSpecBuilder.addFunction(it) }
    fileSpecBuilder
        .addType(objectSpecBuilder.build())
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
    "typeof")
