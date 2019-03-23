package sample.kotlin

import testaccessors.RequiresAccessor

internal class KotlinClass<A, B, C> {
	@RequiresAccessor(requires = [RequiresAccessor.AccessorType.TYPE_SETTER])
	private val aField = null
	class StaticInnerClass<T, J : Set<List<T>>, Q> {
		@RequiresAccessor(name = "middleField")
		private val anotherField = emptySet<Q>()
		inner class InnerClass<A> {
			@RequiresAccessor
			private val yetAnotherField = Unit
		}
	}
}
