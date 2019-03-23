package testaccessors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Signal that this field should have accessors generated for it.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface RequiresAccessor {
    /**
     * Optionally specify a name for the accessors generated for this field. Generated accessors will both have the same
     * name as the field, if this field is not set, or the value of the field if set.
     */
    String name() default "";

    /**
     * Optional array of accessors type that need to be generated. By default, both getter and setter will be generated.
     */
    AccessorType[] requires() default {AccessorType.TYPE_GETTER, AccessorType.TYPE_SETTER};

    enum AccessorType {
        TYPE_GETTER,
        TYPE_SETTER
    }
}
