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
  public void setAField() throws NoSuchFieldException, IllegalAccessException {
    final String expected = "this is a mock value";

    AndroidApplicationTestAccessors.aField(subject, expected);

    final Field field = subject.getClass().getDeclaredField("aField");
    final boolean wasAccessible = field.isAccessible();
    field.setAccessible(true);
    final String actual = (String) field.get(subject);
    assertSame(expected, actual);
    field.setAccessible(wasAccessible);
  }

  @Test
  public void getAField() throws NoSuchFieldException, IllegalAccessException {
    final String expected = "this is a mock value";
    final Field field = subject.getClass().getDeclaredField("aField");
    final boolean wasAccessible = field.isAccessible();
    field.setAccessible(true);
    field.set(subject, expected);
    field.setAccessible(wasAccessible);

    final String actual = AndroidApplicationTestAccessors.aField(subject);

    assertSame(expected, actual);
  }
}