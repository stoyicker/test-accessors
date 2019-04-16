package root

import android.app.Application
import testaccessors.RequiresAccessor

class AndroidApplication : Application() {
  @RequiresAccessor
  private val aField: String? = null
  private companion object {
    @RequiresAccessor
    private var aFieldInAPrivateCompanionObject: Any? = null
  }

  override fun onCreate() {
    super.onCreate()
    println(aField) // Prevent it from getting trimmed away by ProGuard
  }
}
