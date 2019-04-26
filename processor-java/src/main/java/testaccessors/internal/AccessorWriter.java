package testaccessors.internal;

import androidx.annotation.RestrictTo;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.annotation.processing.Filer;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import testaccessors.RequiresAccessor;

final class AccessorWriter extends AbstractAccessorWriter {
  private static final String PARAMETER_NAME_RECEIVER = "receiver";

  AccessorWriter(
      final Elements elementUtils,
      final Types typeUtils,
      final Lazy<Logger> logger,
      final Options options) {
    super(elementUtils, typeUtils, logger, options);
  }

  @Override
  public void writeAccessorClass(final Set<Element> annotatedElements, final Filer filer) {
    final Element enclosingClassElement = annotatedElements.iterator().next().getEnclosingElement();
    final String[] extractedLocation = extractLocation(enclosingClassElement.getEnclosingElement());
    final String[] location = new String[extractedLocation.length + 1];
    System.arraycopy(extractedLocation, 0, location, 0, extractedLocation.length);
    location[location.length - 1] = enclosingClassElement.getSimpleName().toString();
    final String[] subLocation = new String[location.length - 2];
    System.arraycopy(location, 2, subLocation, 0, subLocation.length);
    final String classAndFileName = nameForGeneratedClassFrom(ClassName.get(location[0], location[1], subLocation)
        .simpleNames());
    final TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(classAndFileName)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
    annotatedElements.stream()
        .flatMap(new Function<Element, Stream<MethodSpec>>() {
          @Override
          public Stream<MethodSpec> apply(final Element element) {
            final RequiresAccessor.AccessorType[] requires = element.getAnnotation(
                RequiresAccessor.class).requires();
            final MethodSpec[] methodSpecs = new MethodSpec[requires.length];
            final Collection<Modifier> modifiers = element.getModifiers();
            final boolean isStatic = modifiers.contains(Modifier.STATIC);
            for (int i = 0; i < requires.length; i++) {
              switch (requires[i]) {
                case TYPE_GETTER:
                  methodSpecs[i] = isStatic ?
                      generateStaticGetterMethodSpec(element) : generateGetterMethodSpec(element);
                  break;
                case TYPE_SETTER:
                  if (isStatic && modifiers.contains(Modifier.FINAL)) {
                    logger.getOrCompute().error(ERROR_MESSAGE_UNSUPPORTED_STATIC_FINAL_SETTER, element);
                    continue;
                  }
                  methodSpecs[i] = isStatic ?
                      generateStaticSetterMethodSpec(element) : generateSetterMethodSpec(element);
                  break;
              }
            }
            return Arrays.stream(methodSpecs);
          }

          private MethodSpec generateStaticGetterMethodSpec(final Element element) {
            return generateCommonGetterMethodSpec(
                element,
                "/javadoc-getter-static.template",
                new Object[]{
                    typeUtils.erasure(element.getEnclosingElement().asType()),
                    element.getSimpleName().toString()},
                null)
                .build();
          }

          private MethodSpec generateGetterMethodSpec(final Element element) {
            return addReceiver(
                generateCommonGetterMethodSpec(
                    element,
                    "/javadoc-getter.template",
                    new Object[]{
                        typeUtils.erasure(element.getEnclosingElement().asType()),
                        element.getSimpleName().toString(),
                        PARAMETER_NAME_RECEIVER},
                    PARAMETER_NAME_RECEIVER), element)
                .build();
          }

          private MethodSpec.Builder generateCommonGetterMethodSpec(
              final Element element,
              final String javadocResource,
              final Object[] javadocArgs,
              final Object receiverLiteral) {
            final TypeVariableName elementType = TypeVariableName.get(TYPE_NAME_VALUE);
            return generateCommonMethodSpec(element)
                .addJavadoc(readAsset(javadocResource), javadocArgs)
                .beginControlFlow("try")
                .addStatement(
                    "final $T field = $T.class.getDeclaredField($S)",
                    Field.class,
                    typeUtils.erasure(element.getEnclosingElement().asType()),
                    element.getSimpleName())
                .addStatement("final $T wasAccessible = field.isAccessible()", boolean.class)
                .addStatement("field.setAccessible(true)")
                .addStatement("final $T ret = ($T) field.get($L)",
                    elementType,
                    elementType,
                    receiverLiteral)
                .addStatement("field.setAccessible(wasAccessible)")
                .addStatement("return ret")
                .nextControlFlow(
                    "catch (final $T | $T e)",
                    NoSuchFieldException.class,
                    IllegalAccessException.class)
                .addStatement("throw new $T(e)", RuntimeException.class)
                .endControlFlow()
                .returns(elementType);
          }

          private MethodSpec generateStaticSetterMethodSpec(final Element element) {
            return generateCommonSetterMethodSpec(
                element,
                "/javadoc-setter-static.template",
                new Object[]{
                    TypeVariableName.get(TYPE_NAME_VALUE),
                    element.getSimpleName().toString(),
                    PARAMETER_NAME_NEW_VALUE},
                null)
                .addParameter(ParameterSpec.builder(
                    TypeVariableName.get(TYPE_NAME_VALUE),
                    PARAMETER_NAME_NEW_VALUE,
                    Modifier.FINAL)
                    .build())
                .build();
          }

          private MethodSpec generateSetterMethodSpec(final Element element) {
            return addReceiver(generateCommonSetterMethodSpec(
                element,
                "/javadoc-setter.template",
                new Object[]{
                    TypeVariableName.get(TYPE_NAME_VALUE),
                    element.getSimpleName().toString(),
                    PARAMETER_NAME_RECEIVER,
                    PARAMETER_NAME_NEW_VALUE},
                PARAMETER_NAME_RECEIVER
            ), element)
                .addParameter(ParameterSpec.builder(
                    TypeVariableName.get(TYPE_NAME_VALUE),
                    PARAMETER_NAME_NEW_VALUE,
                    Modifier.FINAL)
                    .build())
                .build();
          }

          private MethodSpec.Builder generateCommonSetterMethodSpec(
              final Element element,
              final String javadocResource,
              final Object[] javadocArgs,
              final Object receiverLiteral) {
            return generateCommonMethodSpec(element)
                .addJavadoc(
                    readAsset(javadocResource),
                    javadocArgs)
                .beginControlFlow("try")
                .addStatement(
                    "final $T field = $T.class.getDeclaredField($S)",
                    Field.class,
                    typeUtils.erasure(element.getEnclosingElement().asType()),
                    element.getSimpleName())
                .addStatement("final $T wasAccessible = field.isAccessible()", boolean.class)
                .addStatement("field.setAccessible(true)")
                .addStatement("field.set($L, $L)", receiverLiteral, PARAMETER_NAME_NEW_VALUE)
                .addStatement("field.setAccessible(wasAccessible)")
                .nextControlFlow(
                    "catch (final $T | $T e)",
                    NoSuchFieldException.class,
                    IllegalAccessException.class)
                .addStatement("throw new $T(e)", RuntimeException.class)
                .endControlFlow()
                .returns(void.class);
          }

          private String readAsset(final String path) {
            try (final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                AccessorWriter.class.getResourceAsStream(path)))) {
              final StringBuilder ret = new StringBuilder();
              String line;
              while ((line = bufferedReader.readLine()) != null) {
                ret.append(line);
                ret.append('\n');
              }
              return ret.toString();
            } catch (final IOException e) {
              throw new RuntimeException(e);
            }
          }

          private MethodSpec.Builder generateCommonMethodSpec(final Element element) {
            final RequiresAccessor requiresAccessor = element.getAnnotation(RequiresAccessor.class);
            String name = requiresAccessor.name();
            if (!SourceVersion.isName(name)) {
              name = element.getSimpleName().toString();
            }
            final MethodSpec.Builder ret = addSupportRestrictTo(
                addAndroidXRestrictTo(MethodSpec.methodBuilder(name).addModifiers(
                    Modifier.PUBLIC,
                    Modifier.STATIC),
                    requiresAccessor.androidXRestrictTo()),
                requiresAccessor.supportRestrictTo())
                .addTypeVariable(TypeVariableName.get(TYPE_NAME_VALUE));
            final CharSequence requiredPatternInClasspath = options.requiredPatternInClasspath();
            if (requiredPatternInClasspath != null && requiredPatternInClasspath.length() != 0) {
              ret.addCode(CodeBlock.builder()
                  .beginControlFlow(
                      "if (!$T.compile($S).matcher($T.getProperty($S)).find())",
                      Pattern.class, requiredPatternInClasspath,
                      System.class,
                      "java.class.path")
                  .addStatement(
                      "throw new $T($S)",
                      IllegalAccessError.class,
                      ERROR_MESSAGE_ILLEGAL_ACCESS)
                  .endControlFlow()
                  .build());
            }
            return ret;
          }

          private MethodSpec.Builder addAndroidXRestrictTo(
              final MethodSpec.Builder receiver, final RestrictTo annotation) {
            final RestrictTo.Scope[] originalScopes = annotation.value();
            final RestrictTo.Scope[] values = originalScopes.length == 0 ?
                options.defaultAndroidXRestrictTo() : originalScopes;
            final String[] valuesAsStrings = new String[values.length];
            final String prefix = RestrictTo.class.getSimpleName()
                + "."
                + RestrictTo.Scope.class.getSimpleName()
                + ".";
            for (int i = 0; i < values.length; i++) {
              valuesAsStrings[i] = prefix + values[i].name();
            }
            return addAnnotation(
                receiver,
                RestrictTo.class,
                "value",
                valuesAsStrings);
          }

          private MethodSpec.Builder addSupportRestrictTo(
              final MethodSpec.Builder receiver,
              final android.support.annotation.RestrictTo annotation) {
            final android.support.annotation.RestrictTo.Scope[] originalScopes = annotation.value();
            final android.support.annotation.RestrictTo.Scope[] values = originalScopes.length == 0 ?
                options.defaultSupportRestrictTo() : originalScopes;
            final String[] valuesAsStrings = new String[values.length];
            final String prefix = android.support.annotation.RestrictTo.class.getSimpleName()
                + "."
                + android.support.annotation.RestrictTo.Scope.class.getSimpleName()
                + ".";
            for (int i = 0; i < values.length; i++) {
              valuesAsStrings[i] = prefix + values[i].name();
            }
            return addAnnotation(
                receiver,
                android.support.annotation.RestrictTo.class,
                "value",
                valuesAsStrings);
          }

          private MethodSpec.Builder addAnnotation(
              final MethodSpec.Builder receiver,
              final Class<? extends Annotation> annotationClass,
              final String key,
              final String[] values) {
            if (values == null || values.length == 0) {
              return receiver;
            }
            receiver.addAnnotation(AnnotationSpec.builder(annotationClass)
                .addMember(key, CodeBlock.builder()
                    .add("{")
                    .add(String.join(", ", values))
                    .add("}")
                    .build())
                .build());
            return receiver;
          }

          private MethodSpec.Builder addReceiver(
              final MethodSpec.Builder builder, final Element element) {
            builder.addParameter(ParameterSpec.builder(
                TypeName.get(typeUtils.erasure(element.getEnclosingElement().asType())),
                PARAMETER_NAME_RECEIVER,
                Modifier.FINAL)
                .build());
            return builder;
          }
        })
        .forEach(typeSpecBuilder::addMethod);
    try {
      JavaFile.builder(
          elementUtils.getPackageOf(enclosingClassElement).getQualifiedName().toString(),
          typeSpecBuilder.build())
          .indent("  ")
          .build()
          .writeTo(filer);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }
}
