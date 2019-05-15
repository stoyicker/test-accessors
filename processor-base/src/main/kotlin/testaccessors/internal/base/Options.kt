package testaccessors.internal.base

import androidx.annotation.RestrictTo

interface Options {
  fun requiredPatternInClasspath(): CharSequence

  fun defaultAndroidXRestrictTo(): Iterable<RestrictTo.Scope>

  fun defaultSupportRestrictTo(): Iterable<android.support.annotation.RestrictTo.Scope>
}
