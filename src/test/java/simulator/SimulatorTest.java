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


  //ADD
  @Test
  public void testALUMockAdd() {
    simulator.setRegisterValue("$t0", 3);
    simulator.setRegisterValue("$t1", 5);
    Instruction add = Instruction.from("add $v0, $t0, $t1");
    simulator.execute(add);
    assertEquals(simulator.getRegisterValue("$v0"), 8);
  }

  //SUB
  @Test
  public void testALUMockSub() {
    simulator.setRegisterValue("$t0", 5);
    simulator.setRegisterValue("$t1", 3);
    Instruction instruction = Instruction.from("sub $v0, $t0, $t1");
    simulator.execute(instruction);
    assertEquals(simulator.getRegisterValue("$v0"), 2);
  }
  //AND
  //OR
  //NOR
  //SLT
  //LW
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

  //SW
  @Test
  public void testALUMockSW() {
    simulator.setRegisterValue("$t0", 5);
    simulator.setRegisterValue("$t1", 3);
    Instruction instruction = Instruction.from("sw $t0, 20($t1)");
    simulator.execute(instruction);
    assertEquals(simulator.getDataMemory(23), simulator.getRegisterValue("$t0"));
  }

  //BEQ
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

  //ADDI
  @Test
  public void testALUMockADDI() {
    simulator.setRegisterValue("$t1", 0);
    simulator.setRegisterValue("$t2", 3);
    Instruction instruction = Instruction.from("addi $t1, $t2, 4");
    simulator.execute(instruction);
    assertEquals(7, simulator.getRegisterValue("$t1"));
  }
  //ORI
  //SRL
  //SRA
  //J
  @Test
  public void testJInstruction() {
    // 2 is an absolute address. 2 << 2 = 8
    Instruction jump = Instruction.from("j 5");
    Instruction add = Instruction.from("add $v0, $t0, $t1");
    simulator.loadRawProgram(Program.from(
          Instruction.from("lw $t0, 20($t1)"),
          Instruction.from("lw $t0, 21($t1)"),
          Instruction.from("lw $t0, 22($t1)"),
          jump,
          Instruction.from("nop"),
          add,
          Instruction.from("lw $t0, 23($t1)"),
          Instruction.from("lw $t0, 24($t1)"),
          Instruction.from("lw $t0, 25($t1)"
          )));
    simulator.setProgramCounterInstruction(3);
    assertThat(simulator.getCurrentInstruction(), is(equalTo(jump)));
    simulator.executeNextInstruction();
    assertThat(simulator.getCurrentInstruction(), is(equalTo(add)));
  }

  //JR
  @Test
  public void testJRInstruction() {
    // 2 is an absolute address. 2 << 2 = 8

    simulator.setRegisterValue("$t1", 4);
    assertEquals(simulator.getRegisterValue("$t1"), 4);
    Instruction jr = Instruction.from("jr $t1");
    Instruction add= Instruction.from("add $v0, $t0, $t1");
    simulator.loadRawProgram(Program.from(
          Instruction.from("lw $t0, 20($t1)"),
          Instruction.from("lw $t0, 21($t1)"),
          Instruction.from("lw $t0, 22($t1)"),
          Instruction.from("lw $t0, 23($t1)"),
          Instruction.from("lw $t0, 24($t1)"),
          jr,
          Instruction.from("lw $t0, 25($t1)"),
          Instruction.from("lw $t0, 26($t1)"),
          Instruction.from("lw $t0, 27($t1)"),
          add,
          Instruction.from("lw $t0, 28($t1)"),
          Instruction.from("lw $t0, 29($t1)"),
          Instruction.from("lw $t0, 30($t1)"),
          Instruction.from("lw $t0, 31($t1)"),
          Instruction.from("lw $t0, 1($t1)"),
          Instruction.from("lw $t0, 2($t1)"),
          Instruction.from("lw $t0, 3($t1)"),
          Instruction.from("lw $t0, 4($t1)")
    ));
    simulator.setProgramCounterInstruction(5);
    simulator.executeNextInstruction();
    assertThat(simulator.getCurrentInstruction(), is(equalTo(add)));
  }

  //NOP
  @Test
  public void testNOPInstruction() {
    // 2 is an absolute address. 2 << 2 = 8

    simulator.loadRawProgram(Program.from(
          Instruction.from("nop")
    ));
    simulator.executeNextInstruction();
  }













}