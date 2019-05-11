package sample.banana.kotlin

import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ThirdTest {
	private lateinit var subject: First.Second.Third<String>

	@BeforeEach
	fun setUp() {
		subject = First.Second().Third()
	}

	@Test
	fun setAField() {
		val expected = "this is a mock value"

    FirstSecondThirdTestAccessors.yetAnotherField(subject, expected)

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
