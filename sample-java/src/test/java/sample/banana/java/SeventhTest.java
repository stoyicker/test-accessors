package sample.banana.java;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

final class SeventhTest {
  private First.Second.Sixth.Seventh<Object, HashSet<List<?>>, List<?>> subject;

  @BeforeEach
  void setUp() {
    subject = new First.Second().new Sixth(). new Seventh<>();
  }

  @Test
  void setAField() throws NoSuchFieldException, IllegalAccessException {
    @SuppressWarnings("unchecked") final Set<HashSet<List<?>>> expected = mock(Set.class);

    FirstSecondSixthSeventhTestAccessors.fieldThatHasBeenRenamed(subject, expected);

    final Field field = subject.getClass().getDeclaredField("anotherField");
    final boolean wasAccessible = field.isAccessible();
    field.setAccessible(true);
    @SuppressWarnings("unchecked") final Set<HashSet<List<?>>> actual = (Set<HashSet<List<?>>>) field.get(subject);
    assertSame(expected, actual);
    field.setAccessible(wasAccessible);
  }

  @Test
  void getAField() throws NoSuchFieldException, IllegalAccessException {
    @SuppressWarnings("unchecked") final Set<HashSet<List<?>>> expected = mock(Set.class);
    final Field field = subject.getClass().getDeclaredField("anotherField");
    final boolean wasAccessible = field.isAccessible();
    field.setAccessible(true);
    field.set(subject, expected);
    field.setAccessible(wasAccessible);

    final Set< HashSet<List<?>>> actual = FirstSecondSixthSeventhTestAccessors.fieldThatHasBeenRenamed(subject);

    assertSame(expected, actual);
  }

  @Test
  void setAFieldWithWildcardTypes() throws NoSuchFieldException, IllegalAccessException {
    @SuppressWarnings("unchecked")
    final Map<? super Object, Map<? extends Set<List<?>>, ? super Collection<?>>> expected = mock(Map.class);

    FirstSecondSixthSeventhTestAccessors.setFieldWithWildcardTypes(subject, expected);

    final Field field = subject.getClass().getDeclaredField("fieldWithWildcardTypes");
    final boolean wasAccessible = field.isAccessible();
    field.setAccessible(true);
    @SuppressWarnings("unchecked")
    final Map<? super Object, Map<? extends Set<List<?>>, ? super Collection<?>>> actual =
            (Map<? super Object, Map<? extends Set<List<?>>, ? super Collection<?>>>) field.get(subject);
    assertSame(expected, actual);
    field.setAccessible(wasAccessible);
  }

  @Test
  void getAFieldWithWildcardTypes() throws NoSuchFieldException, IllegalAccessException {
    @SuppressWarnings("unchecked")
    final Map<? super Object, Map<? extends Set<List<?>>, ? super Collection<?>>> expected = mock(Map.class);
    final Field field = subject.getClass().getDeclaredField("fieldWithWildcardTypes");
    final boolean wasAccessible = field.isAccessible();
    field.setAccessible(true);
    field.set(subject, expected);
    field.setAccessible(wasAccessible);

    final Map<? super Object, Map<? extends Set<List<?>>, ? super Collection<?>>> actual =
            FirstSecondSixthSeventhTestAccessors.getFieldWithWildcardTypes(subject);

    assertSame(expected, actual);
  }
}
