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

final class AccessorWriter extends AbstractAccessorWriter {
    private final Logger logger;

    AccessorWriter(final Messager messager, final LogLevel logLevel) {
        logger = new Logger(messager, logLevel);
    }

    @Override
    public void writeAccessorClass(final Set<Element> annotatedElements, final Filer filer) {
        final ClassName className = (ClassName) ClassName.get(annotatedElements.iterator().next().getEnclosingElement().asType());
        final TypeSpec.Builder classSpecBuilder = TypeSpec.classBuilder(nameForGeneratedClassFrom(className.simpleNames()))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        annotatedElements.stream()
                .map(new java.util.function.Function<Element, MethodSpec[]>() {
                    @Override
                    public MethodSpec[] apply(final Element element) {
                        final RequiresAccessor.AccessorType[] requires = element.getAnnotation(
                                RequiresAccessor.class).requires();
                        final MethodSpec[] methodSpec = new MethodSpec[requires.length];
                        for (int i = 0; i < requires.length; i++) {
                            if (requires[i] == RequiresAccessor.AccessorType.TYPE_GETTER) {
                                methodSpec[i] = generateGetterMethodSpec(element);
                            } else {
                                methodSpec[i] = generateSetterMethodSpec(element);
                            }
                        }
                        return methodSpec;
                    }

                    private MethodSpec generateGetterMethodSpec(final Element element) {
                        final RequiresAccessor annotation = element.getAnnotation(RequiresAccessor.class);
                        return MethodSpec.methodBuilder(annotation.name().isEmpty() ? element.getSimpleName().toString() : annotation.name())
                                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                .addParameter(ParameterSpec.builder(
                                        className,
                                        PARAMETER_NAME_RECEIVER,
                                        Modifier.FINAL)
                                        .build())
                                .addStatement("return " + element.getSimpleName())
                                .returns(ClassName.get(element.asType()))
                                .build();
                    }

                    private MethodSpec generateSetterMethodSpec(final Element element) {
                        final RequiresAccessor annotation = element.getAnnotation(RequiresAccessor.class);
                        return MethodSpec.methodBuilder(annotation.name().isEmpty() ? element.getSimpleName().toString() : annotation.name())
                                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                .addParameter(ParameterSpec.builder(
                                        className,
                                        PARAMETER_NAME_RECEIVER,
                                        Modifier.FINAL)
                                        .build())
                                .addParameter(ParameterSpec.builder(
                                        ClassName.get(element.asType()),
                                        setterParameterName(),
                                        Modifier.FINAL)
                                        .build())
                                .returns(void.class)
                                .build();
                    }
                })
                .flatMap(Arrays::stream)
                .forEach(classSpecBuilder::addMethod);
        try {
            JavaFile.builder(className.packageName(), classSpecBuilder.build()).build().writeTo(filer);
        } catch (final IOException e) {
            logger.error(Arrays.toString(e.getStackTrace()));
        }
    }

    private static final String PARAMETER_NAME_RECEIVER = "receiver";
}
