package sample.banana.kotlin

import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import sample.banana.kotlin.FirstTestAccessors.aField

internal class FirstTest {
	private lateinit var subject: First<Any?, Any?, Any?, Any?, Any?, Any?, Any?>

	@BeforeEach
	fun setUp() {
		subject = First()
	}

	@Test
	fun setAField() {
		val expected = "this is a mock value"

		subject.aField(expected)

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

		val actual: String? = subject.aField()

		assertSame(expected, actual)
	}

	@Test
	fun setATopLevelField() {
		val expected = "this is a mock value"

		FirstKtTestAccessors.aTopLevelField(expected)

		var actual: Any?
		Class.forName("sample.banana.kotlin.FirstKt").getDeclaredField("aTopLevelField").apply {
			val wasAccessible = isAccessible
			isAccessible = true
			actual = this[null]
			isAccessible = wasAccessible
		}
		assertSame(expected, actual)
	}

	@Test
	fun getATopLevelField() {
		val expected = "this is another mock value"
		Class.forName("sample.banana.kotlin.FirstKt").getDeclaredField("aTopLevelField").apply {
			val wasAccessible = isAccessible
			isAccessible = true
			val modifiersField = Field::class.java.getDeclaredField("modifiers")
			val wasModifiersAccessible = modifiersField.isAccessible
			modifiersField.isAccessible = true
			modifiersField.setInt(this, modifiers and Modifier.FINAL.inv())
			set(null, expected)
			modifiersField.isAccessible = wasModifiersAccessible
			isAccessible = wasAccessible
		}

		val actual: Any? = FirstKtTestAccessors.aTopLevelField()

		assertSame(expected, actual)
	}
}
