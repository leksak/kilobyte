import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.expectThrows;

class AssertionsDemo {

  @Test
  void standardAssertions() {
    assertEquals(2, 2);
    assertEquals(4, 4, "The optional assertion message is now the last parameter.");
    assertTrue(2 == 2, () -> "Assertion messages can be lazily evaluated -- "
          + "to avoid constructing complex messages unnecessarily.");
  }


  @Test
  void exceptionTesting() {
    Throwable exception = expectThrows(IllegalArgumentException.class, () -> {
      throw new IllegalArgumentException("a message");
    });
    assertEquals("a message", exception.getMessage());
  }

}
