package testaccessors.internal;

import com.squareup.javapoet.*;
import testaccessors.RequiresAccessor;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

final class AccessorWriter extends AbstractAccessorWriter {
    private final Logger logger;

    AccessorWriter(final Messager messager, final LogLevel logLevel) {
        logger = new Logger(messager, logLevel);
    }

    @Override
    public void writeAccessorClass(final Set<Element> annotatedElements, final Filer filer) {
        final ClassName enclosingType = (ClassName) TypeName.get(annotatedElements.iterator().next().getEnclosingElement().asType());
        final TypeSpec.Builder classSpecBuilder = TypeSpec.classBuilder(nameForGeneratedClassFrom(enclosingType.simpleNames()))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        annotatedElements.stream()
                .flatMap(new Function<Element, Stream<MethodSpec>>() {
                    @Override
                    public Stream<MethodSpec> apply(final Element element) {
                        final RequiresAccessor.AccessorType[] requires = element.getAnnotation(
                                RequiresAccessor.class).requires();
                        final MethodSpec[] methodSpecs = new MethodSpec[requires.length];
                        for (int i = 0; i < requires.length; i++) {
                            switch (requires[i]) {
                                case TYPE_GETTER:
                                    methodSpecs[i] = generateGetterMethodSpec(element);
                                    break;
                                case TYPE_SETTER:
                                    methodSpecs[i] = generateSetterMethodSpec(element);
                                    break;
                                default:
                                    throw illegalAccessorRequestedException(element);
                            }
                        }
                        return Arrays.stream(methodSpecs);
                    }

                    private MethodSpec generateGetterMethodSpec(final Element element) {
                        final RequiresAccessor annotation = element.getAnnotation(RequiresAccessor.class);
                        return MethodSpec.methodBuilder(annotation.name().isEmpty() ? element.getSimpleName().toString() : annotation.name())
                                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                .addParameter(ParameterSpec.builder(
                                        TypeName.get(element.asType()),
                                        PARAMETER_NAME_RECEIVER,
                                        Modifier.FINAL)
                                        .build())
                                .addStatement("return " + PARAMETER_NAME_RECEIVER + "." + element.getSimpleName())
                                .returns(TypeName.get(element.asType()))
                                .build();
                    }

                    private MethodSpec generateSetterMethodSpec(final Element element) {
                        final TypeName fieldType = TypeName.get(element.asType());
                        final RequiresAccessor annotation = element.getAnnotation(RequiresAccessor.class);
                        return MethodSpec.methodBuilder(annotation.name().isEmpty() ? element.getSimpleName().toString() : annotation.name())
                                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                .addParameter(ParameterSpec.builder(
                                        fieldType,
                                        PARAMETER_NAME_RECEIVER,
                                        Modifier.FINAL)
                                        .build())
                                .addParameter(ParameterSpec.builder(
                                        fieldType,
                                        PARAMETER_NAME_NEW_VALUE,
                                        Modifier.FINAL)
                                        .build())
                                .addStatement(PARAMETER_NAME_RECEIVER + "." + element.getSimpleName() + " = " + PARAMETER_NAME_NEW_VALUE)
                                .returns(void.class)
                                .build();
                    }
                })
                .forEach(classSpecBuilder::addMethod);
        try {
            JavaFile.builder(enclosingType.packageName(), classSpecBuilder.build()).build().writeTo(filer);
        } catch (final IOException e) {
            logger.error(Arrays.toString(e.getStackTrace()));
        }
    }

    private static final String PARAMETER_NAME_RECEIVER = "receiver";
}
