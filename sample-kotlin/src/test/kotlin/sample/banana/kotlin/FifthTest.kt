package sample.banana.kotlin

import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class FifthTest {
	private lateinit var subject: First.Second.Third<Any>.Fourth.Fifth<String>

	@BeforeEach
	fun setUp() {
		subject = First.Second().Third<Any>().Fourth().Fifth()
	}

	@Test
	fun setAField() {
		val expected = mock(Set::class.java) as Set<String>

		FirstSecondThirdFourthFifthTestAccessors.yetAnotherField(subject, expected)

		var actual: Set<String>
		subject::class.java.getDeclaredField("yetAnotherField").apply {
			val wasAccessible = isAccessible
			isAccessible = true
			actual = this[subject] as Set<String>
			isAccessible = wasAccessible
		}
		assertSame(expected, actual)
	}

	@Test
	fun getAField() {
		val expected = mock(Set::class.java) as Set<String>
		subject.javaClass.getDeclaredField("yetAnotherField").apply {
			val wasAccessible = isAccessible
			isAccessible = true
			set(subject, expected)
			isAccessible = wasAccessible
		}

		val actual: Set<String> = FirstSecondThirdFourthFifthTestAccessors.yetAnotherField(subject)

		assertSame(expected, actual)
	}
}
