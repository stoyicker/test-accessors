package root;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertSame;

public final class AndroidApplicationTest {
  private AndroidApplication subject;

  @Before
  public void setUp() {
    subject = new AndroidApplication();
  }

  @Test
  public void setAField() {
    final String expected = "this is a mock value";

    AndroidApplicationTestAccessors.aField(subject, expected);

    final Field field;
    try {
      field = subject.getClass().getDeclaredField("aField");
    } catch (final NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
    final boolean wasAccessible = field.isAccessible();
    field.setAccessible(true);
    final String actual;
    try {
      actual = (String) field.get(subject);
    } catch (final IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    assertSame(expected, actual);
    field.setAccessible(wasAccessible);
  }

  @Test
  public void getAField() {
    final String expected = "this is a mock value";
    final Field field;
    try {
      field = subject.getClass().getDeclaredField("aField");
    } catch (final NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
    final boolean wasAccessible = field.isAccessible();
    field.setAccessible(true);
    try {
      field.set(subject, expected);
    } catch (final IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    field.setAccessible(wasAccessible);

    final String actual = AndroidApplicationTestAccessors.aField(subject);

    assertSame(expected, actual);
  }
}