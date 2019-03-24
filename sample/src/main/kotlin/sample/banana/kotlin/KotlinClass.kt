package sample.banana.kotlin

import testaccessors.RequiresAccessor

class First<A, B, C, D, E, F, G> {
	@RequiresAccessor
	private val aField: String? = null
	private val anotherTopLevelField: String? = null

	class Second {
		inner class Third<A> {
			@RequiresAccessor(requires = [RequiresAccessor.AccessorType.TYPE_SETTER])
			private val yetAnotherField = Unit
			inner class Fourth {
				@RequiresAccessor(requires = [RequiresAccessor.AccessorType.TYPE_SETTER])
				private val yetAnotherField = Unit
				inner class Fifth<A> {
					@RequiresAccessor(requires = [RequiresAccessor.AccessorType.TYPE_SETTER])
					private val yetAnotherField = Unit
				}
			}
		}
		class Sixth {
			class Seventh<T, J : Set<List<T>>, Q> {
				@RequiresAccessor(name = "middleFieldThatHasBeenRenamed")
				private val anotherField = emptySet<Q>()
			}
		}
	}
}
