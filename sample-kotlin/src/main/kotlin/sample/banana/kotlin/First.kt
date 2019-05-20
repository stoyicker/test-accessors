package sample.banana.kotlin

import testaccessors.RequiresAccessor

@RequiresAccessor(requires = [RequiresAccessor.AccessorType.TYPE_GETTER, RequiresAccessor.AccessorType.TYPE_SETTER])
private val aTopLevelField: String? = null

class First<A, B, C, D, E, F, G> {
  @RequiresAccessor(requires = [RequiresAccessor.AccessorType.TYPE_GETTER, RequiresAccessor.AccessorType.TYPE_SETTER])
  private val aField: String? = null
  private val anotherTopLevelField: String? = null

  class Second {
    inner class Third<B> {
      @RequiresAccessor
      private val yetAnotherField: String? = null

      inner class Fourth {
        @RequiresAccessor
        private val yetAnotherField: B? = null

        inner class Fifth<A> {
          @RequiresAccessor(requires = [RequiresAccessor.AccessorType.TYPE_GETTER, RequiresAccessor.AccessorType.TYPE_SETTER])
          private val yetAnotherField = emptySet<A>()
        }
      }
    }

    inner class Sixth {
      inner class Seventh<T, J : Set<List<*>?>?, Q : Collection<T>> {
        @RequiresAccessor(name = "fieldThatHasBeenRenamed", requires = [RequiresAccessor.AccessorType.TYPE_GETTER, RequiresAccessor.AccessorType.TYPE_SETTER])
        private val anotherField: Set<Q?>? = emptySet()
      }
    }
  }
}
