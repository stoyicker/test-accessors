package root;

import android.app.Application;

import androidx.annotation.RestrictTo;
import testaccessors.RequiresAccessor;

public final class AndroidApplication extends Application {
  @RequiresAccessor(androidXRestrictTo = @RestrictTo({RestrictTo.Scope.TESTS, RestrictTo.Scope.LIBRARY}))
  private final String aField = null;

  @Override
  public void onCreate() {
    super.onCreate();
    System.out.println(aField); // Prevent it from getting trimmed away by ProGuard
  }
}
