package simulator;

import common.instruction.Instruction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import simulator.program.Program;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by jwestin on 2017-01-11.
 */
class SimulatorTestJUnit {
  Simulator simulator;
  @BeforeEach
  public void setUp() {
    simulator = new Simulator();

  }

  @Test
  public void testALUMockAdd() {
    simulator.setRegisterValue("$t0", 3);
    simulator.setRegisterValue("$t1", 5);
    Instruction add = Instruction.from("add $v0, $t0, $t1");
    simulator.execute(add);
    assertEquals(simulator.getRegisterValue("$v0"), 8);
  }
  @Test
  public void testALUMockSub() {
    simulator.setRegisterValue("$t0", 5);
    simulator.setRegisterValue("$t1", 3);
    Instruction instruction = Instruction.from("sub $v0, $t0, $t1");
    simulator.execute(instruction);
    assertEquals(simulator.getRegisterValue("$v0"), 2);
  }


  @Test
  public void testALUMockBEQFalse() {
    int startPC = simulator.getProgramCounter().getAddressPointer();
    simulator.setRegisterValue("$t0", 5);
    simulator.setRegisterValue("$t1", 3);
    Instruction instruction = Instruction.from("beq $t0, $t1, 16");
    simulator.execute(instruction);
    int currentPC = simulator.getProgramCounter().getAddressPointer();
    assertEquals(startPC+4, currentPC);
  }

  @Test
  public void testALUMockBEQTrue() {
    int startPC = simulator.getProgramCounter().getAddressPointer();
    // 0000 0110 6
    // 0001 1000 24
    int branchValue = 6;
    simulator.setRegisterValue("$t0", 5);
    simulator.setRegisterValue("$t1", 5);

    Instruction instruction = Instruction.from("beq $t0, $t1, "+String.valueOf(branchValue));
    simulator.execute(instruction);
    int currentPC = simulator.getProgramCounter().getAddressPointer();

    assertEquals(0+4+(24), currentPC);
  }


  @Test
  public void testALUMockLW() {
    Byte startValue = Byte.valueOf("7");
    simulator.setDataMemoryAtAddress(23, startValue);
    simulator.setRegisterValue("$t0", 5);
    simulator.setRegisterValue("$t1", 3);
    Instruction instruction = Instruction.from("lw $t0, 20($t1)");
    simulator.execute(instruction);
    assertEquals(startValue.intValue(), simulator.getRegisterValue("$t0"));
  }

  @Test
  public void testJInstruction() {
    // 2 is an absolute address. 2 << 2 = 8
    Instruction first = Instruction.from("j 2");
    Instruction second = Instruction.from("nop");
    Instruction third = Instruction.from("add $v0, $t0, $t1");
    simulator.loadProgram(Program.from(first, second, third,
          Instruction.from("lw $t0, 20($t1)"),
          Instruction.from("lw $t0, 21($t1)"),
          Instruction.from("lw $t0, 22($t1)"),
          Instruction.from("lw $t0, 23($t1)"),
          Instruction.from("lw $t0, 24($t1)"),
          Instruction.from("lw $t0, 25($t1)"),
          Instruction.from("lw $t0, 26($t1)"),
          Instruction.from("lw $t0, 27($t1)"),
          Instruction.from("lw $t0, 28($t1)"),
          Instruction.from("lw $t0, 29($t1)"),
          Instruction.from("lw $t0, 30($t1)"),
          Instruction.from("lw $t0, 31($t1)"),
          Instruction.from("lw $t0, 1($t1)"),
          Instruction.from("lw $t0, 2($t1)"),
          Instruction.from("lw $t0, 3($t1)"),
          Instruction.from("lw $t0, 4($t1)")
          ));
    simulator.executeNextInstruction();
    assertThat(simulator.getCurrentInstruction(), is(equalTo(third)));
  }
  @Test
  public void testJRInstruction() {
    // 2 is an absolute address. 2 << 2 = 8

    simulator.setRegisterValue("$t1", 3);
    Instruction first = Instruction.from("jr $t1");
    Instruction second = Instruction.from("nop");
    Instruction third = Instruction.from("add $v0, $t0, $t1");
    simulator.loadProgram(Program.from(first, second, third,
          Instruction.from("lw $t0, 20($t1)"),
          Instruction.from("lw $t0, 21($t1)"),
          Instruction.from("lw $t0, 22($t1)"),
          Instruction.from("lw $t0, 23($t1)"),
          Instruction.from("lw $t0, 24($t1)"),
          Instruction.from("lw $t0, 25($t1)"),
          Instruction.from("lw $t0, 26($t1)"),
          Instruction.from("lw $t0, 27($t1)"),
          Instruction.from("lw $t0, 28($t1)"),
          Instruction.from("lw $t0, 29($t1)"),
          Instruction.from("lw $t0, 30($t1)"),
          Instruction.from("lw $t0, 31($t1)"),
          Instruction.from("lw $t0, 1($t1)"),
          Instruction.from("lw $t0, 2($t1)"),
          Instruction.from("lw $t0, 3($t1)"),
          Instruction.from("lw $t0, 4($t1)")
          ));
    simulator.executeNextInstruction();
    assertThat(simulator.getCurrentInstruction(), is(equalTo(third)));
  }



  @Test
  public void testALUMockSW() {
    simulator.setRegisterValue("$t0", 5);
    simulator.setRegisterValue("$t1", 3);
    Instruction instruction = Instruction.from("sw $t0, 20($t1)");
    simulator.execute(instruction);
    assertEquals(simulator.getDataMemory(23), simulator.getRegisterValue("$t0"));
  }





  @Test
  public void testALUADD() {
    simulator.execute(Instruction.ADD);
  }

  @Test
  public void testALUSUB() {
    simulator.execute(Instruction.SUB);
  }

  @Test
  public void testALUAND() {
    simulator.execute(Instruction.AND);
  }
  @Test
  public void testALULW() {
    //Should add
    simulator.execute(Instruction.LW);
  }
  @Test
  public void testALUSW() {
    //Should add
    simulator.execute(Instruction.SW);
  }
  @Test
  public void testALUBRANSWHEQUAL() {
    //Should subtract
    simulator.execute(Instruction.BEQ);
  }
  @Test
  public void testALUOR() {
    //Should subtract
    simulator.execute(Instruction.OR);
  }
  @Disabled
  public void testALUNOR() {
    //Should NOR -- not implemented
    fail("Not yet implemented");
  }
  @Test
  public void testALUSLT() {
    //Should Set less on
    simulator.execute(Instruction.SLT);
  }
}