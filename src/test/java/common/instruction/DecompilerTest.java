package common.instruction;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DecompilerTest {
  @Test
  void testTranslationOfStringLiteralsToIntegerValuesFromDifferentBases() {
    assertAll(() -> {
      assertEquals(0x71014802, Decompiler.decode("0x71014802"));
      assertEquals(0x71014802, Decompiler.decode("1895909378"));
      assertEquals(0x71014802, Decompiler.decode("0d1895909378"));
      assertEquals(0x71014802, Decompiler.decode("0b1110001000000010100100000000010"));
    });
  }

  @Test
  void testThatPrefixesAreTreatedAsCaseInsensitive() {
    assertAll(() -> {
      assertEquals(0x71014802, Decompiler.decode("0X71014802"));
      assertEquals(0x71014802, Decompiler.decode("0D1895909378"));
    });
  }

  @Nested
  class IntegrationTests {
    @Test
    void testThatReadingASingleLineFromAFileYieldsTheAppropriateInstruction() {

    }
  }
}
