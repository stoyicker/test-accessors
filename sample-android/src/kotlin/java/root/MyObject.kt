package root

import testaccessors.RequiresAccessor

object MyObject {
  @RequiresAccessor(requires = [RequiresAccessor.AccessorType.TYPE_GETTER, RequiresAccessor.AccessorType.TYPE_SETTER])
  private const val myField = "holahola"
}
