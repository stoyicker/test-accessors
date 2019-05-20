package root;

import android.app.Application;

import androidx.annotation.RestrictTo;
import testaccessors.RequiresAccessor;

public final class AndroidApplication extends Application {
  @RequiresAccessor(
      requires = {RequiresAccessor.AccessorType.TYPE_GETTER, RequiresAccessor.AccessorType.TYPE_SETTER},
      androidXRestrictTo = @RestrictTo({RestrictTo.Scope.TESTS, RestrictTo.Scope.LIBRARY}))
  private final String aField = "holahola";
  @RequiresAccessor(
      requires = {RequiresAccessor.AccessorType.TYPE_GETTER, RequiresAccessor.AccessorType.TYPE_SETTER},
      androidXRestrictTo = @RestrictTo(RestrictTo.Scope.TESTS))
  private static Object aStaticField = null;
}
