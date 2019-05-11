package sample.banana.kotlin

import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import java.util.HashSet

internal class SeventhTest {
	private lateinit var subject: First.Second.Sixth.Seventh<Any, HashSet<List<Any?>>, List<Any>>

	@BeforeEach
	fun setUp() {
		subject = First.Second().Sixth().Seventh()
	}

	@Test
	fun setAField() {
		@Suppress("UNCHECKED_CAST") val expected = mock(Set::class.java) as Set<HashSet<List<Any>>>

    FirstSecondSixthSeventhTestAccessors.fieldThatHasBeenRenamed(subject, expected)

		var actual: Set<HashSet<List<Any>>>
		subject::class.java.getDeclaredField("anotherField").apply {
			val wasAccessible = isAccessible
			isAccessible = true
			@Suppress("UNCHECKED_CAST")
			actual = this[subject] as Set<HashSet<List<Any>>>
			isAccessible = wasAccessible
		}
		assertSame(expected, actual)
	}

	@Test
	fun getAField() {
		@Suppress("UNCHECKED_CAST") val expected = mock(Set::class.java) as Set<HashSet<List<Any>>>
		subject.javaClass.getDeclaredField("anotherField").apply {
			val wasAccessible = isAccessible
			isAccessible = true
			set(subject, expected)
			isAccessible = wasAccessible
		}

		val actual: Set<String> = FirstSecondSixthSeventhTestAccessors.fieldThatHasBeenRenamed(subject)

		assertSame(expected, actual)
	}
}
