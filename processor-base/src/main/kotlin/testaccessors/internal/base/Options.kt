package testaccessors.internal.base

import androidx.annotation.RestrictTo

interface Options {
  fun requiredClasses(): List<String>

  fun defaultAndroidXRestrictTo(): Iterable<RestrictTo.Scope>

  fun defaultSupportRestrictTo(): Iterable<android.support.annotation.RestrictTo.Scope>
}
