package testaccessors

import androidx.annotation.RestrictTo

/**
 * Signal that this field should have accessors generated for it.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
annotation class RequiresAccessor(
    /**
     * Optionally specify a name for the accessors generated for this field. Generated accessors will both have the same
     * name as the field, if this field is not set, or the value of the field if set.
     */
    val name: String = "",
    /**
     * Optional array of accessors type that need to be generated. By default both getter and setter will be generated.
     */
    val requires: Array<AccessorType> = [AccessorType.TYPE_GETTER, AccessorType.TYPE_SETTER],
    /**
     * Optional instance of [RestrictTo] that will be copied to the generated accessors. By default no annotation is copied.
     */
    val androidXRestrictTo: RestrictTo = RestrictTo(),
    /**
     * Optional instance of [android.support.annotation.RestrictTo] that will be copied to the generated accessors.
     * By default no annotation is copied.
     */
    val supportRestrictTo: android.support.annotation.RestrictTo = android.support.annotation.RestrictTo()) {
  enum class AccessorType {
    TYPE_GETTER,
    TYPE_SETTER
  }
}
