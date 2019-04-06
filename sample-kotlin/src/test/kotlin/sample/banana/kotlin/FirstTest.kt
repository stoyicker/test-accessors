package sample.banana.kotlin

import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import sample.banana.kotlin.FirstTestAccessors.aField

class FirstTest {
	private lateinit var subject: First<Any?, Any?, Any?, Any?, Any?, Any?, Any?>

	@BeforeEach
	fun setUp() {
		subject = First()
	}

	@Test
	fun setAField() {
		val expected = "this is a mock value"

		subject.aField(expected)

		val actual = subject::class.java.getDeclaredField("aField")[subject]
		assertSame(expected, actual)
	}
}
