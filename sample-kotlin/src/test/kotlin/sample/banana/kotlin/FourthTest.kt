package sample.banana.kotlin

import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FourthTest {
	private lateinit var subject: First.Second.Third<String>.Fourth

	@BeforeEach
	fun setUp() {
		subject = First.Second().Third<String>().Fourth()
	}

	@Test
	fun setAField() {
		val expected = "this is a mock value"

    FirstSecondThirdFourthTestAccessors.yetAnotherField(subject, expected)

		var actual: String?
		subject::class.java.getDeclaredField("yetAnotherField").apply {
			val wasAccessible = isAccessible
			isAccessible = true
			actual = this[subject] as String?
			isAccessible = wasAccessible
		}
		assertSame(expected, actual)
	}
}
