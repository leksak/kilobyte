package common.instruction;

import common.hardware.Register;
import common.instruction.decomposedrepresentation.DecomposedRepresentation;
import common.instruction.exceptions.NoSuchInstructionException;
import io.atlassian.fugue.Either;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.*;

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

  @Nested
  class TestInstantiationForAtLeastOneInstructionFromEachPattern {
    @Test
    void INAME_RS() throws Exception {
      assertThat(Instruction.JR, is(equalTo(Instruction.from("jr $t1"))));
      assertThat(Instruction.JR, is(equalTo(Instruction.unsafeFrom(0x01200008))));
    }

    @Test
    void INAME_RD_RS() throws Exception {
      assertThat(Instruction.JALR, is(equalTo(Instruction.from("jalr $t1, $t2"))));
      assertThat(Instruction.JALR, is(equalTo(Instruction.unsafeFrom(0x01404809))));
    }
  }


  @Test
  @DisplayName("Creating an instruction from 0x00 yields \"NOP\"")
  void gettingTheNOPInstructionFromAllZeroes() throws NoSuchInstructionException {
    Instruction actual = Instruction.unsafeFrom(0x00);
    assertEquals(Instruction.NOP, actual);
  }

  @Test
  @DisplayName("All (long, String) archetype-pairs yield matching instructions")
  void checkThatAllPrototypesCreateEqualInstancesFromTheirRespectiveExamples() throws Exception {
    for (Example e : Instruction.allExamples()) {
      String mnemonic = e.getMnemonicExample();
      long numeric = e.getNumericExample();
      Instruction fromMnemonic = Instruction.from(mnemonic);
      Instruction fromNumeric = Instruction.unsafeFrom(numeric);
      assertEquals(fromMnemonic, fromNumeric);
    }
  }

  @Test
  @DisplayName("A \"NoSuchInstructionException\" is thrown when expected")
  void testNoSuchInstructionExceptionIsThrownOnUnknown32BitInteger() {
    expectThrows(NoSuchInstructionException.class, () -> Instruction.unsafeFrom(0xFFFFFF));
  }


  @Test
  void testInameRs() throws Exception {
    Instruction mnemonic = Instruction.from("jr $t1");
    assertEquals(Instruction.JR, mnemonic, "Failed to translate from the mnemonic representation");

    Instruction instructionNumeric = Instruction.unsafeFrom(0x01200008);
    assertEquals(Instruction.JR, instructionNumeric, "Failed to translate from the numeric representation");

    assertEquals(mnemonic, instructionNumeric);
  }

  @Test
  void testMaddInstruction() throws Exception {
    Instruction mnemonic = Instruction.from("madd $t1, $t2");
    assertEquals(Instruction.MADD, mnemonic, "Failed to translate from the mnemonic representation");

    Instruction instructionNumeric = Instruction.unsafeFrom(0x712A0000);
    assertEquals(Instruction.MADD, instructionNumeric, "Failed to translate from the numeric representation");

    assertEquals(mnemonic, instructionNumeric);
  }
  @Test
  void testSWInstruction() throws Exception {
    Instruction mnemonic = Instruction.from("sw $ra, 4($sp)");
    assertEquals(Instruction.SW, mnemonic, "Failed to translate from the mnemonic representation");

    Instruction instructionNumeric = Instruction.unsafeFrom(0xAFBF0004);
    assertEquals(Instruction.SW, instructionNumeric, "Failed to translate from the numeric representation");

    assertEquals(mnemonic, instructionNumeric);
  }
  @Test
  void testPrefixInstruction() throws Exception {
    Instruction mnemonic = Instruction.from("pref 1, 2($sp)");
    assertEquals(Instruction.PREF, mnemonic, "Failed to translate from the mnemonic representation");

    Instruction instructionNumeric = Instruction.unsafeFrom(0xCFA10002);
    assertEquals(Instruction.PREF, instructionNumeric, "Failed to translate from the numeric representation");

    assertEquals(mnemonic, instructionNumeric);
  }

  @Test
  void testHexInstruction() throws Exception {
    String s = "pref 0x01, 0x02($sp)";
    Instruction mnemonic = Instruction.from(s);
    assertEquals(Instruction.PREF, mnemonic, "Failed to translate from the mnemonic representation \"" + s + "\"");

    Instruction instructionNumeric = Instruction.unsafeFrom(0xCFA10002);
    assertEquals(Instruction.PREF, instructionNumeric, "Failed to translate from the numeric representation");

    assertEquals(mnemonic, instructionNumeric);
  }

  @Nested
  @DisplayName("Testing that two's complement is handled nicely across immediate (signed/unsigned) instructions")
  class SignednessTests {
    @Test
    void testAddi() throws NoSuchInstructionException {
      String source = "addi $sp, $sp, -8"; // From the course website
      long instruction = 0x23bdfff8;

      int opcode = Instruction.ADDI.getOpcode();
      int rt = Register.$sp.asInt();
      int rs = Register.$sp.asInt();
      int immediate = (int) Integer.toUnsignedLong(-8) & 0xffff;

      int expected = opcode << 26 | rt << 21 | rs << 16 | immediate;
      if (expected - instruction != 0) throw new IllegalStateException("These two should be equal");

    }
  }
}
