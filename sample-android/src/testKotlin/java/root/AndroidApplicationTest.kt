package root

import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import root.AndroidApplicationTestAccessors.aField

class AndroidApplicationTest {
  private lateinit var subject: AndroidApplication

  @Before
  fun setUp() {
    subject = AndroidApplication()
  }

  @Test
  fun setAField() {
    val expected = "this is a mock value"

    subject.aField(expected)

    lateinit var actual: String
    subject::class.java.getDeclaredField("aField").apply {
      val wasAccessible = isAccessible
      isAccessible = true
      actual = this[subject] as String
      isAccessible = wasAccessible
    }
    assertSame(expected, actual)
  }

  @Test
  fun getAField() {
    val expected = "this is a mock value"
    subject::class.java.getDeclaredField("aField").apply {
      val wasAccessible = isAccessible
      isAccessible = true
      set(subject, expected)
      isAccessible = wasAccessible
    }

    val actual = subject.aField()

    assertSame(expected, actual)
  }
}