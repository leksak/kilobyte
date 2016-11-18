package common.instruction;

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

  @Nested
  class PartiallyLegalInstructionsTests {
    void assertIsPartiallyValidInstruction(Either<Instruction, PartiallyValidInstruction> e) {
      assertTrue(e.isRight(), "Expected a partially valid instruction. Got: " + e);
    }

    void assertIsPartiallyValidInstruction(long machineCode) throws NoSuchInstructionException {
      assertIsPartiallyValidInstruction(Instruction.from(machineCode));
    }

    @Test
    @DisplayName("\"iname rd, rs\"-instructions are partially legal when rt!=0")
    void testThatInstructionsOnTheForm_INAME_RD_RS_areInvalidIf_RT_isNotZero()
          throws Exception {
      // JALR is expressed on the form "iname rd, rs".
      long numberWithNonZeroRT = Instruction.JALR.getNumericRepresentation();
      numberWithNonZeroRT |= (4 << 16); // Add 4 in the "rt" field.
      assertIsPartiallyValidInstruction(Instruction.from(numberWithNonZeroRT));
    }

    @Nested
    @DisplayName("mflo/mfhi-instructions are partially legal when rs,rt,shamt!=0")
    class testThat_MFLO_and_MFHI_ArePartiallyLegalWhenRtRsOrShamtIsNonZero {
      int rs = 1 << 21;
      int rt = 2 << 16;
      int shamt = 3 << 6;
      long mflo = Instruction.MFLO.getNumericRepresentation();
      long mfhi = Instruction.MFHI.getNumericRepresentation();

      @Test
      @DisplayName("mflo is partially legal when rs,rt,shamt or any combination thereof is not zero")
      void mfloTests() throws NoSuchInstructionException {
        assertAll(
              () -> assertIsPartiallyValidInstruction(mflo | rs),
              () -> assertIsPartiallyValidInstruction(mflo | rt),
              () -> assertIsPartiallyValidInstruction(mflo | shamt),
              () -> assertIsPartiallyValidInstruction(mflo | rs | rt),
              () -> assertIsPartiallyValidInstruction(mflo | rs | shamt),
              () -> assertIsPartiallyValidInstruction(mflo | rt | shamt),
              () -> assertIsPartiallyValidInstruction(mflo | rt | shamt | rs)
        );
      }

      @Test
      @DisplayName("mfhi is partially legal when rs,rt,shamt or any combination thereof is not zero")
      void mfhiTests() throws NoSuchInstructionException {
        assertAll(
              () -> assertIsPartiallyValidInstruction(mfhi | rs),
              () -> assertIsPartiallyValidInstruction(mfhi | rt),
              () -> assertIsPartiallyValidInstruction(mfhi | shamt),
              () -> assertIsPartiallyValidInstruction(mfhi | rs | rt),
              () -> assertIsPartiallyValidInstruction(mfhi | rs | shamt),
              () -> assertIsPartiallyValidInstruction(mfhi | rt | shamt),
              () -> assertIsPartiallyValidInstruction(mfhi | rt | shamt | rs)
        );
      }
    }


    @Nested
    @DisplayName("mtlo/mthi-instructions are partially legal when rt,rd,shamt!=0")
    class testThat_MTLO_and_MTHI_ArePartiallyLegalWhenRtRsOrShamtIsNonZero {
      int rt = 2 << 16;
      int rd = 1 << 11;
      int shamt = 3 << 6;
      long mtlo = Instruction.MTLO.getNumericRepresentation();
      long mthi = Instruction.MTHI.getNumericRepresentation();

      @Test
      @DisplayName("mtlo is partially legal when rt,rd,shamt or any combination thereof is not zero")
      void mtloTests() throws NoSuchInstructionException {
        assertAll(
              () -> assertIsPartiallyValidInstruction(mtlo | rd),
              () -> assertIsPartiallyValidInstruction(mtlo | rt),
              () -> assertIsPartiallyValidInstruction(mtlo | shamt),
              () -> assertIsPartiallyValidInstruction(mtlo | rd | rt),
              () -> assertIsPartiallyValidInstruction(mtlo | rd | shamt),
              () -> assertIsPartiallyValidInstruction(mtlo | rt | shamt),
              () -> assertIsPartiallyValidInstruction(mtlo | rt | shamt | rd)
        );
      }

      @Test
      @DisplayName("mthi is partially legal when rt,rd,shamt or any combination thereof is not zero")
      void mthiTests() throws NoSuchInstructionException {
        assertAll(
              () -> assertIsPartiallyValidInstruction(mthi | rd),
              () -> assertIsPartiallyValidInstruction(mthi | rt),
              () -> assertIsPartiallyValidInstruction(mthi | shamt),
              () -> assertIsPartiallyValidInstruction(mthi | rd | rt),
              () -> assertIsPartiallyValidInstruction(mthi | rd | shamt),
              () -> assertIsPartiallyValidInstruction(mthi | rt | shamt),
              () -> assertIsPartiallyValidInstruction(mthi | rt | shamt | rd)
        );
        System.out.println(Instruction.from(mthi | rt | shamt | rd));
      }
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
}
