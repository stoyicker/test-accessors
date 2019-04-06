package testaccessors.internal;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import testaccessors.RequiresAccessor;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class AccessorWriter extends AbstractAccessorWriter {
    AccessorWriter(final Elements elementUtils) {
        super(elementUtils);
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
                        for (int i = 0; i < requires.length; i++) {
                            switch (requires[i]) {
                                case TYPE_GETTER:
                                    methodSpecs[i] = generateGetterMethodSpec(element);
                                    break;
                                case TYPE_SETTER:
                                    methodSpecs[i] = generateSetterMethodSpec(element);
                                    break;
                            }
                        }
                        return Arrays.stream(methodSpecs);
                    }

                    private MethodSpec generateGetterMethodSpec(final Element element) {
                        return generateCommonMethodSpec(element)
                                .addStatement("return " + PARAMETER_NAME_RECEIVER + "." + element.getSimpleName())
                                .returns(TypeName.get(element.asType()))
                                .build();
                    }

                    private MethodSpec generateSetterMethodSpec(final Element element) {
                        return generateCommonMethodSpec(element)
                                .addParameter(ParameterSpec.builder(
                                        TypeName.get(element.asType()),
                                        PARAMETER_NAME_NEW_VALUE,
                                        Modifier.FINAL)
                                        .build())
                                .addStatement(PARAMETER_NAME_RECEIVER + "." + element.getSimpleName() + " = " + PARAMETER_NAME_NEW_VALUE)
                                .returns(void.class)
                                .build();
                    }

                    private MethodSpec.Builder generateCommonMethodSpec(final Element element) {
                        final RequiresAccessor annotation = element.getAnnotation(RequiresAccessor.class);
                        return addReceiver(MethodSpec.methodBuilder(
                                annotation.name().isEmpty() ? element.getSimpleName().toString() : annotation.name())
                                .addModifiers(Modifier.PUBLIC, Modifier.STATIC), element);
                    }

                    private MethodSpec.Builder addReceiver(final MethodSpec.Builder builder, final Element element) {
                        builder.addParameter(ParameterSpec.builder(
                                TypeName.get(element.getEnclosingElement().asType()),
                                PARAMETER_NAME_RECEIVER,
                                Modifier.FINAL)
                                .build());
                        final List<Element> enclosingElementList = enclosingElementsOf(element);
                        builder.addTypeVariables(
                        enclosingElementList.stream()
                                .filter(it -> enclosingElementList.get(0) == it ||
                                (!it.getModifiers().contains(Modifier.STATIC)
                                        && it.getEnclosingElement().getKind() != ElementKind.PACKAGE))
                        .map(it -> TypeName.get(it.asType()))
                        .filter(it -> it instanceof ParameterizedTypeName)
                        .flatMap(it -> ((ParameterizedTypeName) it).typeArguments.stream())
                        .distinct()
                        .map(it -> TypeVariableName.get(
                                it.toString(),
                                (((TypeVariableName) it).bounds.toArray(new TypeName[0]))))
                        .collect(Collectors.toList()));
                        return builder;
                    }

                    private List<Element> enclosingElementsOf(final Element element) {
                        Element eachEnclosing = element.getEnclosingElement();
                        final List<Element> ret = new LinkedList<>();
                        while (eachEnclosing != null && eachEnclosing.getKind() != ElementKind.PACKAGE) {
                            ret.add(eachEnclosing);
                            eachEnclosing = eachEnclosing.getEnclosingElement();
                        }
                        return ret;
                    }
                })
                .forEach(typeSpecBuilder::addMethod);
        try {
            JavaFile.builder(
                    elementUtils.getPackageOf(enclosingClassElement).getQualifiedName().toString(),
                    typeSpecBuilder.build()).build().writeTo(filer);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String PARAMETER_NAME_RECEIVER = "receiver";
}
