package testaccessors;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import androidx.annotation.RestrictTo;

/**
 * Signal that this field should have accessors generated for it.
 */
@Retention(RetentionPolicy.SOURCE)
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

  RestrictTo androidXRestrictTo() default @RestrictTo(RestrictTo.Scope.TESTS);

  android.support.annotation.RestrictTo supportRestrictTo()
      default @android.support.annotation.RestrictTo(
          android.support.annotation.RestrictTo.Scope.TESTS);

  Class<? extends Annotation>[] customAnnotations() default {};

  enum AccessorType {
    TYPE_GETTER,
    TYPE_SETTER
  }
}
