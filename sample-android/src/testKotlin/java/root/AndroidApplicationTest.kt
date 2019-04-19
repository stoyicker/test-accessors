package root

import org.junit.After
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class AndroidApplicationTest {
  private lateinit var subject: AndroidApplication

  @Before
  fun setUp() {
    subject = AndroidApplication()
  }

  @After
  fun tearDown() {
    AndroidApplication::class.java.getDeclaredField("aFieldInAPrivateCompanionObject").apply {
      val wasAccessible = isAccessible
      isAccessible = true
      set(subject, null)
      isAccessible = wasAccessible
    }
  }

  @Test
  fun setAField() {
    val expected = "this is another mock value"

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
    subject::class.java.getDeclaredField("aField").apply {
      val wasAccessible = isAccessible
      isAccessible = true
      set(subject, expected)
      isAccessible = wasAccessible
    }

    val actual = subject.aField()

    assertSame(expected, actual)
  }

  @Test
  fun setAFieldInAPrivateCompanionObject() {
    val expected = "this is a mock value"

    AndroidApplication::class.aFieldInAPrivateCompanionObject(expected)

    var actual: Any?
    AndroidApplication::class.java.getDeclaredField("aFieldInAPrivateCompanionObject").apply {
      val wasAccessible = isAccessible
      isAccessible = true
      actual = this[null]
      isAccessible = wasAccessible
    }
    assertSame(expected, actual)
  }

  @Test
  fun getAFieldInAPrivateCompanionObject() {
    val expected = "this is another mock value"
    AndroidApplication::class.java.getDeclaredField("aFieldInAPrivateCompanionObject").apply {
      val wasAccessible = isAccessible
      isAccessible = true
      set(null, expected)
      isAccessible = wasAccessible
    }

    val actual = AndroidApplication::class.aFieldInAPrivateCompanionObject()

    assertSame(expected, actual)
  }
}