package root

import android.app.Application
import androidx.annotation.RestrictTo

import testaccessors.RequiresAccessor

class AndroidApplication : Application() {
  @RequiresAccessor(androidXRestrictTo = RestrictTo(RestrictTo.Scope.LIBRARY_GROUP))
  private val aField: String? = null

  override fun onCreate() {
    super.onCreate()
    println(aField) // Prevent it from getting trimmed away by ProGuard
  }
}
