package testaccessors.internal;

import androidx.annotation.RestrictTo;

interface Options {
  CharSequence requiredPatternInClasspath();

  RestrictTo.Scope[] defaultAndroidXRestrictTo();

  android.support.annotation.RestrictTo.Scope[] defaultSupportRestrictTo();
}
