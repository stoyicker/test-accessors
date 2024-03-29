package sample.banana.java;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

final class FifthTest {
  private First.Second.Third.Fourth.Fifth subject;

  @BeforeEach
  void setUp() {
    subject = new First.Second().new Third<>().new Fourth().new Fifth<String>();
  }

  @Test
  void setAField() throws NoSuchFieldException, IllegalAccessException {
    @SuppressWarnings("unchecked") final Set<String> expected = mock(Set.class);

    FirstSecondThirdFourthFifthTestAccessors.setYetAnotherField(subject, expected);

    final Field field = subject.getClass().getDeclaredField("yetAnotherField");
    final boolean wasAccessible = field.isAccessible();
    field.setAccessible(true);
    @SuppressWarnings("unchecked") final Set<String> actual = (Set<String>) field.get(subject);
    assertSame(expected, actual);
    field.setAccessible(wasAccessible);
  }

  @Test
  void getAField() throws NoSuchFieldException, IllegalAccessException {
    @SuppressWarnings("unchecked") final Set<String> expected = mock(Set.class);
    final Field field = subject.getClass().getDeclaredField("yetAnotherField");
    final boolean wasAccessible = field.isAccessible();
    field.setAccessible(true);
    field.set(subject, expected);
    field.setAccessible(wasAccessible);

    final Set<String> actual = FirstSecondThirdFourthFifthTestAccessors.getYetAnotherField(subject);

    assertSame(expected, actual);
  }
}
