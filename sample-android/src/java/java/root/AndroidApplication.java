package root;

import android.app.Application;

import androidx.annotation.RestrictTo;
import testaccessors.RequiresAccessor;

public final class AndroidApplication extends Application {
  @RequiresAccessor(androidXRestrictTo = @RestrictTo({RestrictTo.Scope.TESTS, RestrictTo.Scope.LIBRARY}))
  private final String aField = "holahola";
  @RequiresAccessor(androidXRestrictTo = @RestrictTo(RestrictTo.Scope.TESTS))
  private static Object aStaticField = null;
}
