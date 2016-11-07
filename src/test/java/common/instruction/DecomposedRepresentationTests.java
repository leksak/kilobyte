package common.instruction;

import common.instruction.decomposedrepresentation.DecomposedRepresentation;
import lombok.val;
import org.junit.jupiter.api.Test;

import static common.instruction.decomposedrepresentation.DecomposedRepresentation.fromNumber;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
