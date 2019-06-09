package sample.banana.kotlin

import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import sample.banana.kotlin.FirstSecondThirdFourthFifthTestAccessors.yetAnotherField

internal class FifthTest {
	private lateinit var subject: First.Second.Third<Any>.Fourth.Fifth<String>

	@BeforeEach
	fun setUp() {
		subject = First.Second().Third<Any>().Fourth().Fifth()
	}

	@Test
	fun setAField() {
		@Suppress("UNCHECKED_CAST") val expected = mock(Set::class.java) as Set<String>

		subject.yetAnotherField(expected)

		var actual: Set<String>
		subject::class.java.getDeclaredField("yetAnotherField").apply {
			val wasAccessible = isAccessible
			isAccessible = true
			@Suppress("UNCHECKED_CAST")
			actual = this[subject] as Set<String>
			isAccessible = wasAccessible
		}
		assertSame(expected, actual)
	}

	@Test
	fun getAField() {
		@Suppress("UNCHECKED_CAST") val expected = mock(Set::class.java) as Set<String>
		subject.javaClass.getDeclaredField("yetAnotherField").apply {
			val wasAccessible = isAccessible
			isAccessible = true
			set(subject, expected)
			isAccessible = wasAccessible
		}

		val actual: Set<String> = subject.yetAnotherField()

		assertSame(expected, actual)
	}
}
