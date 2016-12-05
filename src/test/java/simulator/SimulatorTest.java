package simulator;

import com.google.common.collect.ImmutableSet;
import common.hardware.Register;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static common.hardware.Register.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class SimulatorTest {

  Simulator s;
  @BeforeEach
  void initCreateSimulator() {
    s = new Simulator();
  }

  @AfterEach
  void tearDown() {
    s = null;
  }

  @Test
  void checkRegisterFileVsRegister() {
    $t2.value = 55;
    ImmutableSet<Register> registers = ImmutableSet.copyOf(Register.values());
    assertThat(55, is(equalTo(registers.asList().get(10).value)));
  }

  @Test
  void testAddExecutionPath() {
    $t2.value = 1;
    $t3.value = 2;

    s.execute("add $t1, $t2, $t3");
    assertThat($t1, is(equalTo(3)));
  }

  @Test
  void testSubExecutionPath() {
    $t2.value = 5;
    $t3.value = 3;

    s.execute("sub $t1, $t2, $t3");
    assertThat($t1, is(equalTo(2)));
  }

  @Test
  void testAndExecutionPath() {
    // 00001
    $t2.value = 1;
    // 00100
    $t3.value = 4;

    // t1 = t2 & t3 = 00101_2 (5_10)
    s.execute("and $t1, $t2, $t3");
    assertThat($t1, is(equalTo(5)));
  }

  @Test
  void testOrExecutionPath() {
    // Bitwise OR : Set $t1 to bitwise OR of $t2 and $t3
    // 00110
    $t2.value = 6;
    // 00101
    $t3.value = 5;

    // t1 = t2 | t3 = 00111_2 (7_10)

    s.execute("or $t1, $t2, $t3");
    assertThat($t1, is(equalTo(7)));
  }

  @Test
  void testNorExecutionPath() {
    // Bitwise NOR : Set $t1 to bitwise NOR of $t2 and $t3
    // 00110
    $t2.value = 6;
    // 00101
    $t3.value = 5;

    //TODO:
    // t1 = t2 | t3 = _2 (_10)

    s.execute("nor $t1, $t2, $t3");
    //assertThat($t1, is(equalTo()));
  }

  @Test
  void testSLTTrueExecutionPath() {
    // Set less than : If $t2 is less than $t3, then set $t1 to 1 else set $t1 to 0
    $t2.value = 1;
    $t3.value = 2;

    // t1 = t2 < t3 = 1_2 (true)
    s.execute("slt $t1, $t2, $t3");
    assertThat($t1, is(equalTo(1)));
  }

  @Test
  void testSLTFalseExecutionPath() {
    // Set less than : If $t2 is less than $t3, then set $t1 to 1 else set $t1 to 0
    $t2.value = 2;
    $t3.value = 1;

    // t1 = t2 < t3 = 0_2 (false)
    s.execute("slt $t1, $t2, $t3");
    assertThat($t1, is(equalTo(0)));
  }

  @Test
  void testSLTEqualFalseExecutionPath() {
    // Set less than : If $t2 is less than $t3, then set $t1 to 1 else set $t1 to 0
    $t2.value = 2;
    $t3.value = 2;

    // t1 = t2 < t3 = 0_2 (false)
    s.execute("slt $t1, $t2, $t3");
    assertThat($t1, is(equalTo(0)));
  }

  @Test
  void testADDIExecutionPath() {
    // Addition immediate with overflow : set $t1 to ($10 plus signed 16-bit immediate)
    //$10 == $t2
    $t2.value = 5;

    //t1 == 5 + 4 == 9_10
    s.execute("addi $t1, $10, 4");
    assertThat($t1, is(equalTo(9)));
  }

  @Test
  void testORIExecutionPath() {
    // Bitwise OR immediate : Set $t1 to bitwise OR of $t2 and zero-extended 16-bit immediate
    //01001
    $t2.value = 9;

    //t1 == 9(01001) OR 4(00100) == 13(01101)
    s.execute("ori $t1, $t2, 4");
    assertThat($t1, is(equalTo(13)));
  }

  @Test
  void testSRLOneExecutionPath() {
    // Shift right logical : Set $t1 to result of shifting $t2 right
    // by number of bits specified by immediate
    //00100
    $t2.value = 4;
    $t0.value = 1;


    //t1 == t2 << t0 (4 << 1) == 8
    s.execute("srl $t1, $t2, $t0");
    assertThat($t1, is(equalTo(8)));
  }

  @Test
  void testSRLTwoExecutionPath() {
    // Shift right logical : Set $t1 to result of shifting $t2 right
    // by number of bits specified by immediate
    //00100
    $t2.value = 4;
    $t0.value = 2;


    //t1 == t2 << t0 (4 << 2) == 16
    s.execute("srl $t1, $t2, $t0");
    assertThat($t1, is(equalTo(16)));
  }


  /**
   LW,
   SW,
   BEQ,
   SRA,
   J,
   JR,
   NOP
   */


}
