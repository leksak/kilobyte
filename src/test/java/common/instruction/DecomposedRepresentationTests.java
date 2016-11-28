package common.instruction;

import common.instruction.decomposedrepresentation.DecomposedRepresentation;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static common.instruction.decomposedrepresentation.DecomposedRepresentation.fromIntArray;
import static common.instruction.decomposedrepresentation.DecomposedRepresentation.fromNumber;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DecomposedRepresentationTests {
  @Test
  void testBitSelection() {
    int testNumber = 0b00000001010010110100100000100000;
    // We'll select these 4 bits --^--^
    int expected =               0b1011;
    assertThat(DecomposedRepresentation.bits(19, 16, testNumber), is(equalTo(expected)));
  }

  @Test
  void testDecimalDecomposition() {
    DecomposedRepresentation d = fromNumber(0x71014802, 6, 5, 5, 5, 5, 6);
    assertEquals("[28 8 1 9 0 2]", d.asDecimalString());
  }

  @Test
  void testSignedness() {
    DecomposedRepresentation d1 = fromNumber(0x23bdfff8, 6, 5, 5, 16);
    DecomposedRepresentation d2 = fromIntArray(new int[] { 8, 29, 29, -8}, 6, 5, 5, 16);
    assertEquals(d1, d2);
  }

  @Test
  void toIntArrayTest() {
    DecomposedRepresentation d = fromNumber(0x71014802, 6, 5, 5, 5, 5, 6);
    assertTrue(Arrays.equals(d.toIntArray(), new int[] {0x1c, 8, 1, 9, 0, 2}));
  }
}
