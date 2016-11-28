package common.instruction;

import decompiler.Decoder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DecompilerTest {
  @Test
  void testTranslationOfStringLiteralsToIntegerValuesFromDifferentBases() {
    assertAll(() -> {
      assertEquals(0x71014802, Decoder.decode("0x71014802"), "Failed to decode hexadecimal string");
      assertEquals(0x71014802, Decoder.decode("1895909378"), "Failed to decode implicit decimal string");
      assertEquals(0x71014802, Decoder.decode("0d1895909378"), "Failed to decode explicit decimal string");
      assertEquals(0x71014802, Decoder.decode("0b1110001000000010100100000000010"), "Failed to decode binary string");
    });
  }

  @Test
  void testThatPrefixesAreTreatedAsCaseInsensitive() {
    assertAll(() -> {
      assertEquals(0x71014802, Decoder.decode("0X71014802"));
      assertEquals(0x71014802, Decoder.decode("0D1895909378"));
    });
  }
}
