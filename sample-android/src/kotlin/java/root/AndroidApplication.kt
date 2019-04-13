package root

import android.app.Application
import androidx.annotation.RestrictTo

import testaccessors.RequiresAccessor

class AndroidApplication : Application() {
  @RequiresAccessor
  private val aField: String? = null

  override fun onCreate() {
    super.onCreate()
    println(aField) // Prevent it from getting trimmed away by ProGuard
  }
}
