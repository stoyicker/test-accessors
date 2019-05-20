package root

import android.app.Application
import testaccessors.RequiresAccessor

@RequiresAccessor(requires = [RequiresAccessor.AccessorType.TYPE_GETTER, RequiresAccessor.AccessorType.TYPE_SETTER])
private val aTopLevelField: String? = null

class AndroidApplication : Application() {
  @RequiresAccessor(requires = [RequiresAccessor.AccessorType.TYPE_GETTER, RequiresAccessor.AccessorType.TYPE_SETTER])
  private val aField: String? = null
  private companion object {
    @RequiresAccessor(requires = [RequiresAccessor.AccessorType.TYPE_GETTER, RequiresAccessor.AccessorType.TYPE_SETTER])
    private var aFieldInAPrivateCompanionObject: Any? = null
  }
}
