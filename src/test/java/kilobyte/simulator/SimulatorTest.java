package kilobyte.simulator;

import kilobyte.common.instruction.Instruction;
import kilobyte.simulator.program.Program;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class SimulatorTest {
  /**
   * Addition with overflow. Put the
   * sum of registers $t0 and $t1 into register
   * $v0. 
   */
  @Test
  public void testALUMockAdd() {
    val simulator = Simulator.withInstructionsInMemory("add $v0, $t0, $t1");
    simulator.setRegisterValue("$t0", 3);
    simulator.setRegisterValue("$t1", 5);
    simulator.executeNextInstruction();
    assertEquals(simulator.getRegisterValue("$v0"), 8);
  }


  /**
   * Subtraction with overflow: set $v0 to ($t0 minus $t1)
   */
  @Test
  public void testALUMockSub() {
    val simulator = Simulator.withInstructionsInMemory("sub $v0, $t0, $t1");
    simulator.setRegisterValue("$t0", 5);
    simulator.setRegisterValue("$t1", 3);
    simulator.executeNextInstruction();
    assertEquals(simulator.getRegisterValue("$v0"), 2);
  }

  /**
   * Bitwise AND: Set $v0 to bitwise AND (&) of $t0 and $t1
   */
  @Test
  public void testALUMockAND() {
    val simulator = Simulator.withInstructionsInMemory(
          "and $v0, $t0, $t1",
          "and $v0, $t2, $t1"
    );

    simulator.setRegisterValue("$t0", 0);
    simulator.setRegisterValue("$t1", 1);
    simulator.setRegisterValue("$t2", 3);
    simulator.executeNextInstruction();
    assertEquals(0, simulator.getRegisterValue("$v0"));
    simulator.executeNextInstruction();
    assertEquals(1, simulator.getRegisterValue("$v0"));
  }

  /**
   * Bitwise OR: Set $v0 to bitwise OR (|) of $t0 and $t1
   */
  @Test
  public void testALUMockOR() {
    val simulator = Simulator.withInstructionsInMemory("or $v0, $t0, $t1");
    simulator.setRegisterValue("$t0", 5);
    simulator.setRegisterValue("$t1", 3);
    simulator.executeNextInstruction();
    assertEquals((5 | 3), simulator.getRegisterValue("$v0"));
  }

  /**
   * Set $v0 to bitwise NOR of $t1 and $t2
   */
  @Test
  public void testALUMockNOR() {
    val simulator = Simulator.withInstructionsInMemory(
          "nor $v0, $t1, $t2",
          "nor $v1, $t2, $t3");
    simulator.setRegisterValue("$t1", 10);
    simulator.setRegisterValue("$t2", 17);
    simulator.setRegisterValue("$t3", 23);

    simulator.executeNextInstruction();
    assertEquals(~(10 | 17), simulator.getRegisterValue("$v0"));
    simulator.executeNextInstruction();
    assertEquals(~(17 | 23), simulator.getRegisterValue("$v1"));
  }
  //NOR
  @Test
  public void testALUMockNOR2() {
    val simulator = Simulator.withInstructionsInMemory("nor $v1, $t2, $t3");
    simulator.setRegisterValue("$t1", 0);
    simulator.setRegisterValue("$t2", 0);
    simulator.executeNextInstruction();
    assertEquals(-1, simulator.getRegisterValue("$v1"));
  }


  //SLT Set less than : If $t1 is less than $t2, then set $v0 to 1 else set $v0 to 0
  @Test
  public void testALUMockSLT() {
    val simulator = Simulator.withInstructionsInMemory(
          "slt $v0, $t1, $t2",
          "slt $v0, $t2, $t1",
          "slt $v1, $t1, $t1"
    );
    simulator.setRegisterValue("$t1", 1);
    simulator.setRegisterValue("$t2", 2);
    simulator.setRegisterValue("$t3", 1);

    simulator.executeNextInstruction();
    assertEquals(1, simulator.getRegisterValue("$v0"));

    simulator.executeNextInstruction();
    assertEquals(0, simulator.getRegisterValue("$v0"));

    simulator.executeNextInstruction();
    assertEquals(0, simulator.getRegisterValue("$v1"));
  }

  /**
   * Load word: Set $t0 to contents of effective memory word address
   */
  @Test
  public void testALUMockLW() {
    Byte startValue = Byte.valueOf("7");
    val simulator = Simulator.withInstructionsInMemory(
          "lw $t0, 20($t1)"
    );
    simulator.setRegisterValue("$t1", 3);
    simulator.setDataMemoryAtAddress(23, startValue);
    simulator.executeNextInstruction();
    assertEquals(startValue.intValue(), simulator.getRegisterValue("$t0"));
  }

  /**
   * Store the word from register $t0 at offset
   */
  @Test
  public void testALUMockSW() {
    val simulator = Simulator.withInstructionsInMemory("sw $t0, 20($t1)");
    simulator.setRegisterValue("$t0", 5);
    simulator.setRegisterValue("$t1", 3);
    
    simulator.executeNextInstruction();
    
    assertEquals(simulator.getRegisterValue("$t0"), 5);
    assertEquals(simulator.getDataMemory(23), simulator.getRegisterValue("$t0"));
  }

  @Test
  public void testALUMockSW2() {
    val simulator = Simulator.withInstructionsInMemory("sw $t0, 0($sp)");
    simulator.setRegisterValue("$t0", 22);
    simulator.setRegisterValue("$sp", 0);
    simulator.executeNextInstruction();
    
    assertEquals(simulator.getRegisterValue("$t0"), 22);
    assertEquals(simulator.getDataMemory(0), simulator.getRegisterValue("$t0"));
  }

  /**
   * Branch if equal: Branch to statement at the address of the label if \$t0 and \$t1 are equal",
   */
  @Test
  public void testALUMockBEQFalse() {
    val simulator = Simulator.withInstructionsInMemory("beq $t0, $t1, 16");
    int startPC = simulator.getProgramCounter().getAddressPointer();
    simulator.setRegisterValue("$t0", 5);
    simulator.setRegisterValue("$t1", 3);
    simulator.executeNextInstruction();
    int currentPC = simulator.getProgramCounter().getAddressPointer();
    assertEquals(startPC + 4, currentPC);
  }

  @Test
  public void testALUMockBEQTrue() {
    val simulator = Simulator.withInstructionsInMemory("beq $t0, $t1, 6");
    simulator.setRegisterValue("$t0", 5);
    simulator.setRegisterValue("$t1", 5);
    simulator.executeNextInstruction();
    int currentPC = simulator.getProgramCounter().getAddressPointer();

    assertEquals(24, currentPC);
  }

  /**
   * Addition immediate with overflow: set $t1 to (\$t2 plus the signed 16-bit immediate)
   */
  @Test
  public void testALUMockADDI() {
    val simulator = Simulator.withInstructionsInMemory("addi $t1, $t2, 4");
    simulator.setRegisterValue("$t1", 0);
    simulator.setRegisterValue("$t2", 3);
    simulator.executeNextInstruction();
    assertEquals(7, simulator.getRegisterValue("$t1"));
  }

  //ORI - Bitwise OR immediate : Set \$t1 to bitwise OR of \$t2 and
  // zero-extended 16-bit immediate: ori \$t1, \$t2, 4
  @Test
  public void testALUMockORI() {
    val simulator = Simulator.withInstructionsInMemory("ori $t1, $t2, 4");
    simulator.setRegisterValue("$t2", 8);
    simulator.executeNextInstruction();
    assertEquals((8 | 4), simulator.getRegisterValue("$t1"));
  }

  /**
   * SRL - Shift right logical: Set $t1 to result of shifting $t2 right by
   * number of bits specified by the immediate
   */
  @Test
  public void testALUMockSRL() {
    val simulator = Simulator.withInstructionsInMemory("srl $t1, $t2, 2");
    val registerValue = 0b01111111; // 127
    val expected = registerValue >>> 2; // >>> 2 is the logical shift right operator
    simulator.setRegisterValue("$t2", registerValue);
    simulator.executeNextInstruction();
    assertEquals(expected, simulator.getRegisterValue("$t1"));
  }

  //SRA - "Shift right arithmetic : Set \$t1 to result of sign-extended
  // shifting \$t2 right by number of bits specified by immediate:sra $t1, $t2, 10
  @Test
  public void testALUMockSRA() {
    val simulator = Simulator.withInstructionsInMemory("sra $t1, $t2, 2");
    val registerValue = 0b01111111; // 127
    val expected = registerValue >> 2; // >> 2 is the arithmetic shift right operator
    simulator.setRegisterValue("$t2", registerValue);
    simulator.executeNextInstruction();
    assertEquals(expected, simulator.getRegisterValue("$t1"));
  }
  @Test
  public void testALUMockSRA2() {
    val simulator = Simulator.withInstructionsInMemory("sra $t1, $t2, 2");
    // 10000000 >> 2 = 11100000
    // 11100000
    simulator.setRegisterValue("$t2", 0b10000000);
    simulator.executeNextInstruction();
    assertEquals((0b10000000 >> 2), simulator.getRegisterValue("$t1"));
  }

  //J
  @Test
  public void testJInstruction() {
    // 2 is an absolute address. 2 << 2 = 8
    val simulator = new Simulator();
    Instruction jump = Instruction.from("j 5");
    Instruction add = Instruction.from("add $v0, $t0, $t1");
    simulator.loadProgram(Program.from(
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

    val simulator = new Simulator();
    simulator.setRegisterValue("$t1", 4);
    Instruction jr = Instruction.from("jr $t1");
    Instruction add= Instruction.from("add $v0, $t0, $t1");
    simulator.loadProgram(Program.from(
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
  public void executingANOPInstructionIncrementsThePC() {
    val simulator = Simulator.withInstructionsInMemory("nop");
    simulator.executeNextInstruction(); // Check that nothing crashes
    int pc = simulator.getProgramCounter().currentInstructionIndex();
    assertThat(pc, is(equalTo(1)));
  }

  @Test
  public void testADDIADDIADDSWEXIT() {
    // 2 is an absolute address. 2 << 2 = 8

    val simulator = Simulator.executingProgram(Program.from(
      Instruction.from("addi $t1, $zero, 10"),
      Instruction.from("addi $t2, $zero, 12"),
      Instruction.from("add $t0, $t1, $t2"),
      Instruction.from("sw $t0, 0($sp)"),
      Instruction.from("exit")
    ));
    //addi $t1, $zero, 10
    simulator.executeNextInstruction();
    assertEquals(10, simulator.getRegisterValue("$t1"));

    //addi $t2, $zero, 12
    simulator.executeNextInstruction();
    assertEquals(12, simulator.getRegisterValue("$t2"));

    //add $t0, $t1, $t2
    simulator.executeNextInstruction();
    assertEquals(22, simulator.getRegisterValue("$t0"));

    //sw $t0, 0($sp)
    simulator.executeNextInstruction();
    assertEquals(22, simulator.getDataMemory(0));
    //exit
  }

  @Test
  public void testSWWithZeros() {
    // 2 is an absolute address. 2 << 2 = 8

    val simulator = Simulator.executingProgram(Program.from(
      Instruction.from("lw $6, 8($3)"),
      Instruction.from("add $3, $6, $6"),
      Instruction.from("sub $4, $3, $0"),
      Instruction.from("sw $5, 4($2)"),
      Instruction.from("sw $6, 4($3)"),
      Instruction.from("sw $7, 8($4)"),
      Instruction.from("sw $8, 24($5)")
    ));
    //lw $6, 8($3)
    simulator.executeNextInstruction();
    assertEquals(0, simulator.getRegisterValue("$6"));

    //add $3, $6, $6
    simulator.executeNextInstruction();
    assertEquals(0, simulator.getRegisterValue("$3"));

    //sub $4, $3, $0
    simulator.executeNextInstruction();
    assertEquals(0, simulator.getRegisterValue("$3"));

    //sw $5, 4($2)
    simulator.executeNextInstruction();
    assertEquals(0, simulator.getDataMemory(4));

    //sw $6, 4($3)
    simulator.executeNextInstruction();
    assertEquals(0, simulator.getDataMemory(4));

    //sw $7, 8($4)
    simulator.executeNextInstruction();
    assertEquals(0, simulator.getDataMemory(8));

    //sw $8, 24($5)
    simulator.executeNextInstruction();
    assertEquals(0, simulator.getDataMemory(24));


  }
  //NOP
  @Test
  public void testSW3() {
    // 2 is an absolute address. 2 << 2 = 8
    val simulator = Simulator.withInstructionsInMemory("sw $t0, 0($sp)");
    simulator.setRegisterValue("$t0", 22);
    simulator.executeNextInstruction();
    assertEquals(22, simulator.getDataMemory(0));
  }

}
