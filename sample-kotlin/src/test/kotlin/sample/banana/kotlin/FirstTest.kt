package sample.banana.kotlin

import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FirstTest {
	private lateinit var subject: First<Any?, Any?, Any?, Any?, Any?, Any?, Any?>

	@BeforeEach
	fun setUp() {
		subject = First()
	}

	@Test
	fun setAField() {
		val expected = "this is a mock value"

		FirstTestAccessors.aField(subject, expected)

		var actual: String?
		subject::class.java.getDeclaredField("aField").apply {
			val wasAccessible = isAccessible
			isAccessible = true
			actual = this[subject] as String?
			isAccessible = wasAccessible
		}
		assertSame(expected, actual)
	}

	@Test
	fun getAField() {
		val expected = "this is a mock value"
		subject.javaClass.getDeclaredField("aField").apply {
			val wasAccessible = isAccessible
			isAccessible = true
			set(subject, expected)
			isAccessible = wasAccessible
		}

		val actual: String? = FirstTestAccessors.aField(subject)

		assertSame(expected, actual)
	}
}
