package root

import testaccessors.RequiresAccessor

object MyObject {
  @RequiresAccessor
  private const val myField = "holahola"
}
