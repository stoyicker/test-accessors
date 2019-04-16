package sample.banana.java;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertSame;

public class FirstTest {
  private First<Object, Object, Object, Object, Object, Object, Object> subject;

  @BeforeEach
  public void setUp() {
    subject = new First<>();
  }

  @Test
  public void setAField() throws NoSuchFieldException, IllegalAccessException {
    final String expected = "this is a mock value";

    FirstTestAccessors.<Object, Object, Object, Object, Object, Object, Object>aField(subject, expected);

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

    final String actual = FirstTestAccessors.<Object, Object, Object, Object, Object, Object, Object>aField(subject);

//    assertSame(expected, actual);
  }
}
