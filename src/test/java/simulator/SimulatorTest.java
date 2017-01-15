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
  @Test
  public void testALUMockAND() {
    simulator.setRegisterValue("$t0", 0);
    simulator.setRegisterValue("$t1", 1);
    simulator.setRegisterValue("$t2", 3);
    simulator.execute(Instruction.from("and $v0, $t0, $t1"));
    assertEquals(0, simulator.getRegisterValue("$v0"));
    simulator.execute(Instruction.from("and $v0, $t2, $t1"));
    assertEquals(1, simulator.getRegisterValue("$v0"));
  }

  //OR
  @Test
  public void testALUMockOR() {
    simulator.setRegisterValue("$t0", 5);
    simulator.setRegisterValue("$t1", 3);
    Instruction instruction = Instruction.from("sub $v0, $t0, $t1");
    simulator.execute(instruction);
    assertEquals(simulator.getRegisterValue("$v0"), 2);
  }

  //NOR
  @Test
  public void testALUMockNOR() {
    simulator.setRegisterValue("$t1", 10);
    simulator.setRegisterValue("$t2", 17);
    simulator.setRegisterValue("$t3", 23);

    simulator.execute(Instruction.from("nor $v0, $t1, $t2"));
    assertEquals(~(10 | 17), simulator.getRegisterValue("$v0"));
    simulator.execute(Instruction.from("nor $v1, $t2, $t3"));
    assertEquals(~(17 | 23), simulator.getRegisterValue("$v1"));
  }


  //SLT Set less than : If \$t2 is less than $t3, then set $t1 to 1 else set $t1 to 0
  @Test
  public void testALUMockSLT() {
    simulator.setRegisterValue("$t1", 1);
    simulator.setRegisterValue("$t2", 2);
    simulator.setRegisterValue("$t3", 1);

    simulator.execute(Instruction.from("slt $v0, $t1, $t2"));
    assertEquals(1, simulator.getRegisterValue("$v0"));

    simulator.execute(Instruction.from("slt $v0, $t2, $t1"));
    assertEquals(0, simulator.getRegisterValue("$v0"));

    simulator.execute(Instruction.from("slt $v1, $t1, $t1"));
    assertEquals(0, simulator.getRegisterValue("$v1"));

  }

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
  //ORI - Bitwise OR immediate : Set \$t1 to bitwise OR of \$t2 and
  // zero-extended 16-bit immediate: ori \$t1, \$t2, 4
  @Test
  public void testALUMockORI() {
    simulator.setRegisterValue("$t2", 8);
    Instruction instruction = Instruction.from("ori $t1, $t2, 4");
    simulator.execute(instruction);
    assertEquals((8 | 4), simulator.getRegisterValue("$t1"));
  }

  //SRL - Shift right logical : Set \$t1 to result of shifting \$t2 right by
  // number of bits specified by immediate: srl \$t1, \$t2, 10
  @Test
  public void testALUMockSRL() {
    simulator.setRegisterValue("$t2", 127);
    //0111 1111(127_10) >>> 2 = 0001 1111(31_10)

    Instruction instruction = Instruction.from("srl $t1, $t2, 2");
    simulator.execute(instruction);
    assertEquals(31, simulator.getRegisterValue("$t1"));
  }
  @Test
  public void testALUMockSRL2() {
    //10000000 >>> 2 = 00100000
    simulator.setRegisterValue("$t2", 0b10000000);
    Instruction instruction = Instruction.from("srl $t1, $t2, 2");
    simulator.execute(instruction);
    assertEquals(0b00100000, simulator.getRegisterValue("$t1"));
  }

  //SRA - "Shift right arithmetic : Set \$t1 to result of sign-extended
  // shifting \$t2 right by number of bits specified by immediate:sra $t1, $t2, 10
  @Test
  public void testALUMockSRA() {
    simulator.setRegisterValue("$t2", 127);
    // 0111 1111 (127)
    // 0001 1111 (31)
    Instruction instruction = Instruction.from("sra $t1, $t2, 2");
    simulator.execute(instruction);
    assertEquals(31, simulator.getRegisterValue("$t1"));
  }
  @Test
  public void testALUMockSRA2() {
    // 10000000 >> 2 = 11100000
    // 11100000
    simulator.setRegisterValue("$t2", 0b10000000);
    Instruction instruction = Instruction.from("sra $t1, $t2, 2");
    simulator.execute(instruction);
    assertEquals((0b10000000 >> 2), simulator.getRegisterValue("$t1"));
  }

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