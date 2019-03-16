package testaccessors.internal;

import testaccessors.RequiresAccessor;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.util.Set;

public final class RequiresAccessorProcessor extends AnnotationProcessor {
    RequiresAccessorProcessor() {
        super(RequiresAccessor.class);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        // TODO Process java
        return false;
    }
}
