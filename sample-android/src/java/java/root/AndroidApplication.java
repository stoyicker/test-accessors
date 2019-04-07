package root;

import android.app.Application;

import testaccessors.RequiresAccessor;

public final class AndroidApplication extends Application {
  @RequiresAccessor
  private final String aField = null;

  @Override
  public void onCreate() {
    super.onCreate();
    System.out.println(aField); // Prevent it from getting trimmed away by ProGuard
  }
}
