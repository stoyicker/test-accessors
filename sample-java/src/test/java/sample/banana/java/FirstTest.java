package sample.banana.java;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    FirstTestAccesors.aField(subject, expected);

    final String actual = (String) subject.getClass().getDeclaredField("aField").get(subject);
    assertSame(expected, actual);
  }
}
