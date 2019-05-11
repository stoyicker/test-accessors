package sample.banana.kotlin

import testaccessors.RequiresAccessor

object MyObject {
  @RequiresAccessor
  private const val myField = "holahola"
}
