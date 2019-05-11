package sample.banana.kotlin

import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.reflect.Field
import java.lang.reflect.Modifier

internal class MyObjectTest {
  private lateinit var subject: MyObject

  @BeforeEach
  fun setUp() {
    subject = MyObject
  }

  @Test
  fun setMyField() {
    val expected = "this is a mock value"

    MyObjectTestAccessors.myField(expected)

    var actual: Any?
    MyObject::class.java.getDeclaredField("myField").apply {
      val wasAccessible = isAccessible
      isAccessible = true
      actual = this[null]
      isAccessible = wasAccessible
    }
    assertSame(expected, actual)
  }

  @Test
  fun getMyField() {
    val expected = "this is another mock value"
    MyObject::class.java.getDeclaredField("myField").apply {
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

    val actual: Any? = MyObjectTestAccessors.myField()

    assertSame(expected, actual)
  }
}