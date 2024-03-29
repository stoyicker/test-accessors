package sample.banana.java;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertSame;

final class ThirdTest {
  private First.Second.Third<String> subject;

  @BeforeEach
  void setUp() {
    subject = new First.Second().new Third<>();
  }

  @Test
  void setAField() throws NoSuchFieldException, IllegalAccessException {
    final String expected = "this is a mock value";

    FirstSecondThirdTestAccessors.setYetAnotherField(subject, expected);

    final Field field = subject.getClass().getDeclaredField("yetAnotherField");
    final boolean wasAccessible = field.isAccessible();
    field.setAccessible(true);
    final String actual = (String) field.get(subject);
    assertSame(expected, actual);
    field.setAccessible(wasAccessible);
  }
}
