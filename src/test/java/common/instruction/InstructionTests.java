package common.instruction;

import common.instruction.exceptions.NoSuchInstructionException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static common.instruction.Format.*;
import static common.instruction.Type.B;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InstructionTests {
  @Nested
  class MnemonicEqualityTests {
    @Test
    void symbolicAndNonSymbolicRepresentationOfRegistersAreTreatedAsEqual()
          throws NoSuchInstructionException {
      assertThat(Instruction.from("add $9, $10, $11"),
            is(equalTo(Instruction.from("add $t1, $t2, $t3"))));
    }

    // TODO: Test that symbolic instructions handle different bases,
    // i.e. srl $t5, $t5, 2 == srl $t5, $t5, 0x2
  }

  void equalityTest(String mnemonic, Instruction prototype) {
    assertThat(
          "Expected Instruction.from(\"" + mnemonic + "\") to yield Instruction={" + prototype + "}",
          Instruction.from(mnemonic), is(equalTo(prototype)));
  }

  void equalityTest(int numeric, Instruction prototype) {
    assertThat(
          "Expected Instruction.from(" + numeric + ") to yield Instruction={" + prototype + "}",
          Instruction.from(numeric), is(equalTo(prototype)));
  }

  @Nested
  class TestInstantiationForAtLeastOneInstructionFromEachPattern {
    @Test
    void INAME_RS() throws Exception {
      equalityTest(0x01200008, Instruction.JR);
      equalityTest("jr $t1", Instruction.JR);
    }

    @Test
    void INAME_RD_RS() throws Exception {
      assertThat(Instruction.JALR, is(equalTo(Instruction.from("jalr $t1, $t2"))));
      assertThat(Instruction.JALR, is(equalTo(Instruction.from(0x01404809))));
    }
  }


  @Test
  @DisplayName("Creating an instruction from 0x00 yields \"NOP\" and not \"SLL\"")
  void gettingTheNOPInstructionFromAllZeroes() throws NoSuchInstructionException {
    Instruction actual = Instruction.from(0x00);
    assertEquals(Instruction.NOP, actual);
  }

  @Test
  @DisplayName("All (long, String) archetype-pairs yield matching instructions")
  void checkThatAllPrototypesCreateEqualInstancesFromTheirRespectiveExamples() throws Exception {
    for (Example e : Instruction.allExamples()) {
      if (e.getIname().equals("exit")) {
        continue; // "exit" is not decompile-able
      }
      String mnemonic = e.getMnemonicExample();
      long numeric = e.getNumericExample();
      Instruction fromMnemonic = Instruction.from(mnemonic);
      Instruction fromNumeric = Instruction.from(numeric);
      assertEquals(fromMnemonic, fromNumeric);
    }
  }

  @Test
  @DisplayName("There is no instruction matching \"0xFFFFFF\"")
  void testNoSuchInstructionExceptionIsThrownOnUnknown32BitInteger() {
    assertTrue(Instruction.decompile(0xFFFFFF).isUnknown());
  }


  @Test
  void testInameRs() throws Exception {
    Instruction mnemonic = Instruction.from("jr $t1");
    assertEquals(Instruction.JR, mnemonic, "Failed to translate from the mnemonic representation");

    Instruction instructionNumeric = Instruction.from(0x01200008);
    assertEquals(Instruction.JR, instructionNumeric, "Failed to translate from the numeric representation");

    assertEquals(mnemonic, instructionNumeric);
  }

  @Test
  void testMaddInstruction() throws Exception {
    Instruction mnemonic = Instruction.from("madd $t1, $t2");
    assertEquals(Instruction.MADD, mnemonic, "Failed to translate from the mnemonic representation");

    Instruction instructionNumeric = Instruction.from(0x712A0000);
    assertEquals(Instruction.MADD, instructionNumeric, "Failed to translate from the numeric representation");

    assertEquals(mnemonic, instructionNumeric);
  }

  @Test
  void testShamtInstructions() throws Exception {
    Instruction mnemonic = Instruction.from("sll $t1, $t2, 10");
    assertEquals(Instruction.SLL, mnemonic, "Failed to translate from the mnemonic representation");

    Instruction instructionNumeric = Instruction.from(0xa4a80);
    assertEquals(Instruction.SLL, instructionNumeric, "Failed to translate from the numeric representation");

    assertEquals(mnemonic, instructionNumeric);
  }

  @Test
  void testSWInstruction() throws Exception {
    Instruction mnemonic = Instruction.from("sw $ra, 4($sp)");
    assertEquals(Instruction.SW, mnemonic, "Failed to translate from the mnemonic representation");

    Instruction instructionNumeric = Instruction.from(0xAFBF0004);
    assertEquals(Instruction.SW, instructionNumeric, "Failed to translate from the numeric representation");

    assertEquals(mnemonic, instructionNumeric);
  }

  @Test
  void testPrefixInstruction() throws Exception {
    Instruction mnemonic = Instruction.from("pref 1, 2($sp)");
    assertEquals(Instruction.PREF, mnemonic, "Failed to translate from the mnemonic representation");

    Instruction instructionNumeric = Instruction.from(0xCFA10002);
    assertEquals(Instruction.PREF, instructionNumeric, "Failed to translate from the numeric representation");

    assertEquals(mnemonic, instructionNumeric);
  }

  @Test
  void testHexInstruction() throws Exception {
    String s = "pref 0x01, 0x02($sp)";
    Instruction mnemonic = Instruction.from(s);
    assertEquals(Instruction.PREF, mnemonic, "Failed to translate from the mnemonic representation \"" + s + "\"");

    Instruction instructionNumeric = Instruction.from(0xCFA10002);
    assertEquals(Instruction.PREF, instructionNumeric, "Failed to translate from the numeric representation");

    assertEquals(mnemonic, instructionNumeric);
  }

  @Nested
  @DisplayName("Testing that two's complement is handled nicely across immediate (signed/unsigned) instructions")
  class SignednessTests {
    @Test
    void testAddi() throws NoSuchInstructionException {
      String sourceNeg = "addi $sp, $sp, -8"; // From the course website
      long instructionNeg = 0x23bdfff8; // 8 29 29 -8 (-8 = 65528 when unsigned)
      assertEquals(Instruction.from(sourceNeg), Instruction.from(instructionNeg));

      Instruction instructionPos = Instruction.from(0x23bd0008); // 8 29 29 8
      Instruction sourcePos = Instruction.from("addi $sp, $sp, 8");
      assertEquals(instructionPos, sourcePos);

      assertThat(instructionNeg, is(not(equalTo(instructionPos))));
    }
  }

  @Nested
  @DisplayName("Tests checking that our output matches the test-data we've been supplied")
  class TestsAgainstSuppliedTestData {
    Object[][] givenTestData = {
          {0x23bdfff8, I, "[8 29 29 65528]", "[8 0x1d 0x1d 0xfff8]", "addi $sp, $sp, -8"},
          {0xafbf0004, I, "[43 29 31 4]", "[0x2b 0x1d 0x1f 4]", "sw $ra, 4($sp)"},
          {0xafa40000, I, "[43 29 4 0]", "[0x2b 0x1d 4 0]", "sw $a0, 0($sp)"},
          {0x28880001, I, "[10 4 8 1]", "[0xa 4 8 1]", "slti $t0, $a0, 1"},
          {0x20020001, I, "[8 0 2 1]", "[8 0 2 1]", "addi $v0, $zero, 1"},
          {0x23bd0008, I, "[8 29 29 8]", "[8 0x1d 0x1d 8]", "addi $sp, $sp, 8"},
          {0x03e00008, J, "[0 31 0 0 8]", "[0 0x1f 0 0 8]", "jr $ra"},
          {0x2084ffff, I, "[8 4 4 65535]", "[8 4 4 0xffff]", "addi $a0, $a0, -1"},
          {0x8fa40000, I, "[35 29 4 0]", "[0x23 0x1d 4 0]", "lw $a0, 0($sp)"},
          {0x8fbf0004, I, "[35 29 31 4]", "[0x23 0x1d 0x1f 4]", "lw $ra, 4($sp)"},
          {0x23bd0008, I, "[8 29 29 8]", "[8 0x1d 0x1d 8]", "addi $sp, $sp, 8"},
          {0x70821002, R, "[28 4 2 2 0 2]", "[0x1c 4 2 2 0 2]", "mul $v0, $a0, $v0"},
          {0x03e00008, J, "[0 31 0 0 8]", "[0 0x1f 0 0 8]", "jr $ra"},
    };

    @DisplayName("Branch instructions should use an instruction offset, and the program counter will have incremented")
    @Test
    @Disabled
    void testBeq() throws NoSuchInstructionException {
      // We should use instruction offsets, so for
      //
      // 0x11000003
      //
      // should be decompiled into
      //
      // beq $t0, $zero, 4  (note 4 instructions forwards

      Object[] beq = {0x11000003, B, "[4 8 0 3]", "[4 8 0 3]", "beq $t0, $zero, 4"};
      test(beq);
    }

    @Test
    void testJal() throws NoSuchInstructionException {
      // jal specifies an absolute memory address (in bytes) to jump to,
      // but is coded without its last two bits. Hence, the offset should
      // be shifted right
      Object[] jal = {0x0c100000, J, "[3 1048576]", "[3 0x100000]", "jal 0x00400000"};
      test(jal);
    }


    @Test
    void testGivenTestData() throws NoSuchInstructionException {
      for (Object[] instruction : givenTestData) {
        test(instruction);
      }
    }
  }

  void test(Object[] instruction) throws NoSuchInstructionException {
    long machineCode = (long) (int) instruction[0];
    String assemblyCode = (String) instruction[4];
    assertThat(Instruction.from(machineCode), is(equalTo(Instruction.from(assemblyCode))));
  }

  void test(Instruction fromMnemonic, Instruction fromMachineCode) {
    assertThat(fromMnemonic, is(equalTo(fromMachineCode)));
  }

  void test(String mnemonic, int machineCode) {
    test(Instruction.from(mnemonic), Instruction.from(machineCode));
  }
}
