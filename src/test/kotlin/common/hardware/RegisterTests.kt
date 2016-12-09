package common.hardware

import common.hardware.Register
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals

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
