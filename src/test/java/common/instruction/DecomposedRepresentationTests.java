package common.instruction;

import common.instruction.decomposedrepresentation.DecomposedRepresentation;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class DecomposedRepresentationTests {
  @Test
  void testBitSelection() {
    int testNumber = 0b00000001010010110100100000100000;
    // We'll select these 4 bits --^--^
    int expected = 0b1011;
    assertThat(DecomposedRepresentation.bits(19, 16, testNumber), is(equalTo(expected)));
  }
}
