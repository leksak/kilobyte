package common.hardware

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RegisterTests {
  @Test
  internal fun testThatSymbolicAndNonSymbolicRepresentationsAreEquivalent() {
    assertEquals(RegisterFile["\$t1"], RegisterFile["$9"])
  }

  @Test
  internal fun testThatFromStringYieldsCorrectRegisterInstance() {
    assertEquals(RegisterFile["\$t1"], RegisterFile["\$t1"])
  }

  @Test
  internal fun testThatParsingANumberAsAnIndexYieldsTheCorrectRegister() {
    assertEquals(RegisterFile["\$zero"], RegisterFile[0])
  }
}
