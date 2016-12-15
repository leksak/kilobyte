package common.hardware

import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class RegisterTests {
  @Test
  @DisplayName("RegisterFile[\$t1] is equal to RegisterFile[\$9]")
  fun testThatSymbolicAndNonSymbolicRepresentationsAreEquivalent() {
    assertEquals(RegisterFile["\$t1"], RegisterFile["$9"])
  }

  @Test
  @DisplayName("RegisterFile[\$t1] is equal to itself")
  fun testThatTwoSymbolicRepresentationsAreTreatedAsEqual() {
    assertEquals(RegisterFile["\$t1"], RegisterFile["\$t1"])
  }

  @Test
  @DisplayName("RegisterFile[\$0] is equal to itself")
  fun testThatTwoNumericRepresentationsAreTreatedAsEqual() {
    assertEquals(RegisterFile["\$0"], RegisterFile["\$0"])
  }

  @Test
  @DisplayName("RegisterFile[\$zero] equals RegisterFile[0]")
  fun testThatParsingANumberAsAnIndexYieldsTheCorrectRegister() {
    assertEquals(RegisterFile["\$zero"], RegisterFile[0])
  }

  @Test
  @DisplayName("RegisterFiles have their own registers")
  fun registerFilesDoNotShareRegisters() {
    val rf1: RegisterFile = RegisterFile()
    val rf2: RegisterFile = RegisterFile()

    rf1["\$t0"].value = 2
    rf2["\$t0"].value = 3

    assertThat(rf1["\$t0"].value, `is`(not(equalTo(rf2["\$t0"].value))))
  }

  @Test
  @DisplayName("RegisterFile.indexOf(\$t0) equals 8")
  fun testThatIndexOfReturnsTheExpectedValue() {
    assertThat(RegisterFile.indexOf("\$t0"), `is`(equalTo(8)))
  }
}
