package testaccessors.internal

import androidx.annotation.RestrictTo
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.TypeVariableName
import testaccessors.RequiresAccessor
import testaccessors.internal.base.AbstractAccessorWriter
import testaccessors.internal.base.Options
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.reflect.Field
import java.util.Arrays
import javax.annotation.processing.Filer
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import android.support.annotation.RestrictTo as SupportRestrictTo

internal class AccessorWriter(
    elementUtils: Elements,
    typeUtils: Types,
    options: Options) : AbstractAccessorWriter(elementUtils, typeUtils, options) {
  override fun writeAccessorClass(annotatedElements: Set<Element>, filer: Filer) {
    val enclosingClassElement = annotatedElements.iterator().next().enclosingElement
    val extractedLocation = extractLocation(enclosingClassElement.enclosingElement)
    val location = extractedLocation + enclosingClassElement.simpleName.toString()
    val subLocation = location.toList().subList(2, location.size).toTypedArray()
    val fileAndClassName =
        nameForGeneratedClassFrom(ClassName.get(location[0], location[1], *subLocation)
            .simpleNames())
    val typeSpecBuilder = TypeSpec.classBuilder(fileAndClassName)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
    annotatedElements
        .flatMap(object : (Element) -> Iterable<MethodSpec> {
          override fun invoke(element: Element): Iterable<MethodSpec> {
            return element.getAnnotation(RequiresAccessor::class.java).requires.mapNotNull {
              val modifiers = element.modifiers
              val isStatic = modifiers.contains(Modifier.STATIC)
              when (it) {
                RequiresAccessor.AccessorType.TYPE_GETTER -> if (isStatic)
                  generateStaticGetterMethodSpec(element)
                else
                  generateGetterMethodSpec(element)
                RequiresAccessor.AccessorType.TYPE_SETTER ->
                  if (isStatic)
                    generateStaticSetterMethodSpec(element)
                  else
                    generateSetterMethodSpec(element)
              }
            }
          }

          private fun generateStaticGetterMethodSpec(element: Element) =
              generateCommonGetterMethodSpec(
                  element,
                  "/javadoc-getter-static.template",
                  arrayOf(
                      typeUtils.erasure(element.enclosingElement.asType()),
                      element.simpleName.toString()),
                  null)
                  .build()

          private fun generateGetterMethodSpec(element: Element) =
              generateCommonGetterMethodSpec(
                  element,
                  "/javadoc-getter.template",
                  arrayOf(
                      typeUtils.erasure(element.enclosingElement.asType()),
                      element.simpleName.toString(),
                      PARAMETER_NAME_RECEIVER),
                  PARAMETER_NAME_RECEIVER)
                  .addReceiver(element)
                  .build()

          private fun generateCommonGetterMethodSpec(
              element: Element,
              javadocResource: String,
              javadocArgs: Array<Any>,
              receiverLiteral: Any?) = TypeVariableName.get(TYPE_NAME_VALUE).run {
            generateCommonMethodSpec(element)
                .addJavadoc(readAsset(javadocResource), *javadocArgs)
                .beginControlFlow("try")
                .addStatement(
                    "final \$T field = Class.forName(\$S).getDeclaredField(\$S)",
                    Field::class.java,
                    element.enclosingElement.toLoadableClassString(),
                    element.simpleName)
                .addStatement(
                    "final \$T wasAccessible = field.isAccessible()",
                    Boolean::class.javaPrimitiveType)
                .addStatement("field.setAccessible(true)")
                .addStatement("final \$T ret = (\$T) field.get(\$L)",
                    this,
                    this,
                    receiverLiteral)
                .addStatement("field.setAccessible(wasAccessible)")
                .addStatement("return ret")
                .nextControlFlow(
                    "catch (final \$T | \$T | \$T e)",
                    NoSuchFieldException::class.java,
                    IllegalAccessException::class.java,
                    ClassNotFoundException::class.java)
                .addStatement("throw new \$T(e)", RuntimeException::class.java)
                .endControlFlow()
                .returns(this)
          }

          private fun generateStaticSetterMethodSpec(element: Element) =
              generateCommonSetterMethodSpec(
                  element,
                  "/javadoc-setter-static.template",
                  arrayOf(
                      typeUtils.erasure(element.enclosingElement.asType()),
                      element.simpleName.toString(),
                      PARAMETER_NAME_NEW_VALUE),
                  null)
                  .addParameter(ParameterSpec.builder(
                      TypeVariableName.get(TYPE_NAME_VALUE),
                      PARAMETER_NAME_NEW_VALUE,
                      Modifier.FINAL)
                      .build())
                  .build()

          private fun generateSetterMethodSpec(element: Element) =
              generateCommonSetterMethodSpec(
                  element,
                  "/javadoc-setter.template",
                  arrayOf(
                      typeUtils.erasure(element.enclosingElement.asType()),
                      element.simpleName.toString(),
                      PARAMETER_NAME_RECEIVER,
                      PARAMETER_NAME_NEW_VALUE),
                  PARAMETER_NAME_RECEIVER)
                  .addReceiver(element)
                  .addParameter(ParameterSpec.builder(
                      TypeVariableName.get(TYPE_NAME_VALUE),
                      PARAMETER_NAME_NEW_VALUE,
                      Modifier.FINAL)
                      .build())
                  .build()

          private fun generateCommonSetterMethodSpec(
              element: Element,
              javadocResource: String,
              javadocArgs: Array<Any>,
              receiverLiteral: Any?) = generateCommonMethodSpec(element)
              .addJavadoc(
                  readAsset(javadocResource),
                  *javadocArgs)
              .beginControlFlow("try")
              .addStatement(
                  "final \$T field = Class.forName(\$S).getDeclaredField(\$S)",
                  Field::class.java,
                  element.enclosingElement.toLoadableClassString(),
                  element.simpleName)
              .addStatement(
                  "final \$T wasAccessible = field.isAccessible()",
                  Boolean::class.javaPrimitiveType)
              .addStatement("field.setAccessible(true)")
              .addStatement("\$T modifiersField", Field::class.java)
              .beginControlFlow("try")
              .addStatement(
                  "modifiersField = \$T.class.getDeclaredField(\"modifiers\")", Field::class.java)
              .nextControlFlow("catch (final \$T e)", NoSuchFieldException::class.java)
              .addStatement(
                  "modifiersField = \$T.class.getDeclaredField(\"accessFlags\")", Field::class.java)
              .endControlFlow()
              .addStatement(
                  "final \$T wasModifiersAccessible = modifiersField.isAccessible()",
                  Boolean::class.javaPrimitiveType)
              .addStatement("modifiersField.setAccessible(true)")
              .addStatement(
                  "modifiersField.setInt(field, field.getModifiers() & ~\$T.FINAL)",
                  java.lang.reflect.Modifier::class.java)
              .addStatement("field.set(\$L, \$L)", receiverLiteral, PARAMETER_NAME_NEW_VALUE)
              .addStatement("modifiersField.setAccessible(wasModifiersAccessible)")
              .addStatement("field.setAccessible(wasAccessible)")
              .nextControlFlow(
                  "catch (final \$T | \$T | \$T e)",
                  NoSuchFieldException::class.java,
                  IllegalAccessException::class.java,
                  ClassNotFoundException::class.java)
              .addStatement("throw new \$T(e)", RuntimeException::class.java)
              .endControlFlow()
              .returns(Void.TYPE)

          private fun readAsset(path: String) = BufferedReader(InputStreamReader(
              AccessorWriter::class.java.getResourceAsStream(path))).use(BufferedReader::readText)

          private fun generateCommonMethodSpec(element: Element) =
              element.getAnnotation(RequiresAccessor::class.java).run {
                val methodName = if (!SourceVersion.isName(name)) {
                  element.simpleName.toString()
                } else {
                  name
                }
                val ret = MethodSpec.methodBuilder(methodName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addAnnotation(
                        AnnotationSpec.builder(SuppressWarnings::class.java)
                            .addMember("value", "\"unchecked\"")
                            .build()
                    )
                    .addAndroidXRestrictTo(androidXRestrictTo)
                    .addSupportRestrictTo(supportRestrictTo)
                    .addTypeVariable(TypeVariableName.get(TYPE_NAME_VALUE))
                    .apply {
                      options.requiredClasses().takeIf { it.isNotEmpty() }?.let {
                        addStatement("boolean anyRequiredClassFound = false")
                            .beginControlFlow(
                                "for (final String it : \$T.asList(\$L))",
                                Arrays::class.java,
                                it.joinToString(separator = ", ") { "\"$it\"" })
                            .beginControlFlow("try")
                            .addStatement("Class.forName(it)")
                            .addStatement("anyRequiredClassFound = true")
                            .addStatement("break")
                            .nextControlFlow(
                                "catch (final \$T ignored)", ClassNotFoundException::class.java)
                            .endControlFlow()
                            .endControlFlow()
                            .beginControlFlow("if (!anyRequiredClassFound)")
                            .addStatement(
                                "throw new \$T(\$S)",
                                IllegalAccessError::class.java,
                                ERROR_MESSAGE_ILLEGAL_ACCESS)
                            .endControlFlow()
                      }
                    }
                ret
              }

          private fun MethodSpec.Builder.addAndroidXRestrictTo(annotation: RestrictTo) = apply {
            val originalScopes = annotation.value.toList()
            val values = if (originalScopes.isEmpty())
              options.defaultAndroidXRestrictTo()
            else
              originalScopes
            val prefix = "${RestrictTo::class.java.simpleName}.${RestrictTo.Scope::class.java.simpleName}."
            val valuesAsStrings = values.map { prefix + it.name }
            addAnnotation(RestrictTo::class.java, "value", valuesAsStrings)
          }

          private fun MethodSpec.Builder.addSupportRestrictTo(annotation: SupportRestrictTo) = apply {
            val originalScopes = annotation.value.toList()
            val values = if (originalScopes.isEmpty())
              options.defaultSupportRestrictTo()
            else
              originalScopes
            val prefix = "${SupportRestrictTo::class.java.simpleName}.${SupportRestrictTo.Scope::class.java.simpleName}."
            val valuesAsStrings = values.map { prefix + it.name }
            addAnnotation(SupportRestrictTo::class.java, "value", valuesAsStrings)
          }

          private fun MethodSpec.Builder.addAnnotation(
              annotationClass: Class<out Annotation>,
              key: String,
              values: Iterable<String>?) = apply {
            if (values != null && values.any()) {
              addAnnotation(AnnotationSpec.builder(annotationClass)
                  .addMember(key, CodeBlock.builder()
                      .add("{")
                      .add(values.joinToString(", "))
                      .add("}")
                      .build())
                  .build())
            }
          }

          private fun MethodSpec.Builder.addReceiver(element: Element) = apply {
            addParameter(ParameterSpec.builder(
                TypeName.get(typeUtils.erasure(element.enclosingElement.asType())),
                PARAMETER_NAME_RECEIVER,
                Modifier.FINAL)
                .build())
          }
        })
        .forEach { typeSpecBuilder.addMethod(it) }
    try {
      JavaFile.builder(
          elementUtils.getPackageOf(enclosingClassElement).qualifiedName.toString(),
          typeSpecBuilder.build())
          .indent("  ")
          .build()
          .writeTo(filer)
    } catch (e: IOException) {
      throw RuntimeException(e)
    }

  }

  private companion object {
    const val PARAMETER_NAME_RECEIVER = "receiver"
  }
}
