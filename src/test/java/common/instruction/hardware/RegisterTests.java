package common.instruction.hardware;

import common.hardware.Register;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegisterTests {
  @Test
  void testThatSymbolicAndNonSymbolicRepresentationsAreEquivalent() {
    assertEquals(Register.fromString("$t1"), Register.fromString("$9"));
  }

  @Test
  void testThatFromStringYieldsCorrectRegisterInstance() {
    assertAll("equals",
          () -> assertEquals(Register.$t1, Register.fromString("$t1")),
          () -> assertEquals(Register.$t1, Register.fromString("$9"))
    );
  }

  @Test
  void testThatParsingANumberAsAnIndexYieldsTheCorrectRegister() {
    assertEquals(Register.$zero, Register.fromIndex(0));
  }
}
