package simulator

import common.hardware.RegisterFile
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled
class SimulatorTest {
  internal lateinit var s: Simulator
  internal lateinit var rf: RegisterFile

  @BeforeEach
  internal fun initCreateSimulator() {
    s = Simulator()
    rf = s.registerFile
  }

  @Test
  internal fun testAddExecutionPath() {
    rf["\$t2"].value = 1
    rf["\$t3"].value = 2

    s.execute("add \$t1, \$t2, \$t3")
    assertThat(rf["\$t1"].index, `is`(equalTo(3)))
  }

  @Test
  internal fun testSubExecutionPath() {
    rf["\$t2"].value = 5
    rf["\$t3"].value = 3

    s.execute("sub \$t1, \$t2, \$t3")
    assertThat(rf["\$t1"].index, `is`(equalTo(2)))
  }

  @Test
  internal fun testAndExecutionPath() {
    // 00001
    rf["\$t2"].value = 1
    // 00100
    rf["\$t3"].value = 4

    // t1 = t2 & t3 = 00101_2 (5_10)
    s.execute("and \$t1, \$t2, \$t3")
    assertThat(rf["\$t1"].index, `is`(equalTo(5)))
  }

  @Test
  internal fun testOrExecutionPath() {
    // Bitwise OR : Set $t1 to bitwise OR of $t2 and $t3
    // 00110
    rf["\$t2"].value = 6
    // 00101
    rf["\$t3"].value = 5

    // t1 = t2 | t3 = 00111_2 (7_10)

    s.execute("or \$t1, \$t2, \$t3")
    assertThat(rf["\$t1"].index, `is`(equalTo(7)))
  }

  @Test
  internal fun testNorExecutionPath() {
    // Bitwise NOR : Set $t1 to bitwise NOR of $t2 and $t3
    // 00110
    rf["\$t2"].value = 6
    // 00101
    rf["\$t3"].value = 5

    //TODO:
    // t1 = t2 | t3 = _2 (_10)

    s.execute("nor \$t1, \$t2, \$t3")
    //assertThat($t1, is(equalTo()));
  }

  @Test
  internal fun testSLTTrueExecutionPath() {
    // Set less than : If $t2 is less than $t3, then set $t1 to 1 else set $t1 to 0
    rf["\$t2"].value = 1
    rf["\$t3"].value = 2

    // t1 = t2 < t3 = 1_2 (true)
    s.execute("slt \$t1, \$t2, \$t3")
    assertThat(rf["\$t1"].index, `is`(equalTo(1)))
  }

  @Test
  internal fun testSLTFalseExecutionPath() {
    // Set less than : If $t2 is less than $t3, then set $t1 to 1 else set $t1 to 0
    rf["\$t2"].value = 2
    rf["\$t3"].value = 1

    // t1 = t2 < t3 = 0_2 (false)
    s.execute("slt \$t1, \$t2, \$t3")
    assertThat(rf["\$t1"].index, `is`(equalTo(0)))
  }

  @Test
  internal fun testSLTEqualFalseExecutionPath() {
    // Set less than : If $t2 is less than $t3, then set $t1 to 1 else set $t1 to 0
    rf["\$t2"].value = 2
    rf["\$t3"].value = 2

    // t1 = t2 < t3 = 0_2 (false)
    s.execute("slt \$t1, \$t2, \$t3")
    assertThat(rf["\$t1"].index, `is`(equalTo(0)))
  }

  @Test
  internal fun testADDIExecutionPath() {
    // Addition immediate with overflow : set $t1 to ($10 plus signed 16-bit immediate)
    //$10 == $t2
    rf["\$t2"].value = 5

    //t1 == 5 + 4 == 9_10
    s.execute("addi \$t1, $10, 4")
    assertThat(rf["\$t1"].index, `is`(equalTo(9)))
  }

  @Test
  internal fun testORIExecutionPath() {
    // Bitwise OR immediate : Set $t1 to bitwise OR of $t2 and zero-extended 16-bit immediate
    //01001
    rf["\$t2"].value = 9

    //t1 == 9(01001) OR 4(00100) == 13(01101)
    s.execute("ori \$t1, \$t2, 4")
    assertThat(rf["\$t1"].index, `is`(equalTo(13)))
  }

  @Test
  internal fun testSRLOneExecutionPath() {
    // Shift right logical : Set $t1 to result of shifting $t2 right
    // by number of bits specified by immediate
    //00100
    rf["\$t2"].value = 4
    rf["\$t0"].value = 1


    //t1 == t2 << t0 (4 << 1) == 8
    s.execute("srl \$t1, \$t2, \$t0")
    assertThat(rf["\$t1"].index, `is`(equalTo(8)))
  }

  @Test
  internal fun testSRLTwoExecutionPath() {
    // Shift right logical : Set $t1 to result of shifting $t2 right
    // by number of bits specified by immediate
    //00100
    rf["\$t2"].value = 4
    rf["\$t0"].value = 2


    //t1 == t2 << t0 (4 << 2) == 16
    s.execute("srl \$t1, \$t2, \$t0")
    assertThat(rf["\$t1"].index, `is`(equalTo(16)))
  }


  /**
   * LW,
   * SW,
   * BEQ,
   * SRA,
   * J,
   * JR,
   * NOP
   */


}
