package sample.banana.kotlin

import testaccessors.RequiresAccessor

class RootClass<A, B, C, D, E, F, G> {
	@RequiresAccessor
	private val aField: String? = null
	private val anotherTopLevelField: String? = null

	class AbcClass {
		class StaticMiddleClass {
			class StaticInnerClass<T, J : Set<List<T>>, Q> {
				@RequiresAccessor(name = "middleFieldThatHasBeenRenamed")
				private val anotherField = emptySet<Q>()
			}
		}
	}

	inner class InnerClass<A> {
		@RequiresAccessor(requires = [RequiresAccessor.AccessorType.TYPE_SETTER])
		private val yetAnotherField = Unit
	}
}
