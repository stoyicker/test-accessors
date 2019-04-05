package sample.banana.kotlin

import testaccessors.RequiresAccessor

class First<A, B, C, D, E, F, G> {
	@RequiresAccessor(requires = [RequiresAccessor.AccessorType.TYPE_SETTER])
	private val aField: String? = null
	private val anotherTopLevelField: String? = null

	class Second {
		inner class Third<B> {
//			@RequiresAccessor(requires = [RequiresAccessor.AccessorType.TYPE_SETTER])
			private val yetAnotherField = Unit

			inner class Fourth {
//				@RequiresAccessor(requires = [RequiresAccessor.AccessorType.TYPE_SETTER])
				private val yetAnotherField: B? = null

				inner class Fifth<A> {
//					@RequiresAccessor
					private val yetAnotherField = emptySet<A>()
				}
			}
		}

		class Sixth {
			class Seventh<T, J : Set<List<T>>, Q> {
//				@RequiresAccessor(name = "middleFieldThatHasBeenRenamed")
				private val anotherField = emptySet<Q>()
			}
		}
	}
}
