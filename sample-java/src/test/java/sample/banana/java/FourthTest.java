package sample.banana.java;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

final class FourthTest {
  private First.Second.Third.Fourth subject;

  @BeforeEach
  void setUp() {
    subject = new First.Second().new Third<String>().new Fourth();
  }

  @Test
  void setAField() throws NoSuchFieldException, IllegalAccessException {
    final String expected = "this is a mock value";

    FirstSecondThirdFourthTestAccessors.setYetAnotherField(subject, expected);

    final Field field = subject.getClass().getDeclaredField("yetAnotherField");
    final boolean wasAccessible = field.isAccessible();
    field.setAccessible(true);
    final String actual = (String) field.get(subject);
    assertSame(expected, actual);
    field.setAccessible(wasAccessible);
  }

  @Test
  void setNestedTypeField() throws NoSuchFieldException, IllegalAccessException {
    @SuppressWarnings("unchecked") final Map<String, Set<String>> expected = mock(Map.class);

    FirstSecondThirdFourthTestAccessors.setNestedTypeField(subject, expected);

    final Field field = subject.getClass().getDeclaredField("nestedTypeField");
    final boolean wasAccessible = field.isAccessible();
    field.setAccessible(true);
    @SuppressWarnings("unchecked") final Map<String, Set<String>>  actual = (Map<String, Set<String>>) field.get(subject);
    assertSame(expected, actual);
    field.setAccessible(wasAccessible);
  }
}
