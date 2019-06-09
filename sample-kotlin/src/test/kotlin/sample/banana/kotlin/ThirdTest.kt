package sample.banana.kotlin

import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import sample.banana.kotlin.FirstSecondThirdTestAccessors.yetAnotherField

internal class ThirdTest {
	private lateinit var subject: First.Second.Third<String>

	@BeforeEach
	fun setUp() {
		subject = First.Second().Third()
	}

	@Test
	fun setAField() {
		val expected = "this is a mock value"

		subject.yetAnotherField(expected)

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
