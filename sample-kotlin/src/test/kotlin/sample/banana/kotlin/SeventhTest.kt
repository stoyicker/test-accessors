package sample.banana.kotlin

import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import java.util.HashSet

class SeventhTest {
	private lateinit var subject: First.Second.Sixth.Seventh<Any, HashSet<List<Any?>>, List<Any>>

	@BeforeEach
	fun setUp() {
		subject = First.Second().Sixth().Seventh()
	}

	@Test
	fun setAField() {
		val expected = mock(Set::class.java) as Set<HashSet<List<Any>>>

    FirstSecondSixthSeventhTestAccessors.fieldThatHasBeenRenamed(subject, expected)

		var actual: Set<HashSet<List<Any>>>
		subject::class.java.getDeclaredField("anotherField").apply {
			val wasAccessible = isAccessible
			isAccessible = true
			actual = this[subject] as Set<HashSet<List<Any>>>
			isAccessible = wasAccessible
		}
		assertSame(expected, actual)
	}

	@Test
	fun getAField() {
		val expected = mock(Set::class.java) as Set<HashSet<List<Any>>>
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
