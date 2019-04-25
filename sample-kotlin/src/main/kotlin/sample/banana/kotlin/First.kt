package sample.banana.kotlin

import testaccessors.RequiresAccessor

class First<A, B, C, D, E, F, G> {
  @RequiresAccessor
  private val aField: String? = null
  private val anotherTopLevelField: String? = null

  class Second {
    inner class Third<B> {
      @RequiresAccessor(requires = [RequiresAccessor.AccessorType.TYPE_SETTER])
      private val yetAnotherField: String? = null

      inner class Fourth {
        @RequiresAccessor(requires = [RequiresAccessor.AccessorType.TYPE_SETTER])
        private val yetAnotherField: B? = null

        inner class Fifth<A> {
          @RequiresAccessor
          private val yetAnotherField = emptySet<A>()
        }
      }
    }

    inner class Sixth {
      inner class Seventh<T, J : Set<List<*>?>?, Q : Collection<T>> {
        @RequiresAccessor(name = "fieldThatHasBeenRenamed")
        private val anotherField: Set<Q?>? = emptySet()
      }
    }
  }
}
