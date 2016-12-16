package common.instruction;

import common.instruction.decomposedrepresentation.DecomposedRepresentation;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static common.instruction.decomposedrepresentation.DecomposedRepresentation.fromIntArray;
import static common.instruction.decomposedrepresentation.DecomposedRepresentation.fromNumber;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.*;

public class DecomposedRepresentationTests {
  @Test
  void testDecimalDecomposition() {
    DecomposedRepresentation d = fromNumber(0x71014802, 6, 5, 5, 5, 5, 6);
    assertThat(d.asDecimalString()).isEqualTo("[28 8 1 9 0 2]");
  }

  @Test
  void testSignedness() {
    DecomposedRepresentation d1 = fromNumber(0x23bdfff8, 6, 5, 5, 16);
    DecomposedRepresentation d2 = fromIntArray(new int[]{8, 29, 29, -8}, 6, 5, 5, 16);
    assertEquals(d1, d2);
  }

  @Test
  void toIntArrayTest() {
    DecomposedRepresentation d = fromNumber(0x71014802, 6, 5, 5, 5, 5, 6);
    assertTrue(Arrays.equals(d.toIntArray(), new int[]{0x1c, 8, 1, 9, 0, 2}));
  }
}
