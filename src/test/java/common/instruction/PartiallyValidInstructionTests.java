package common.instruction;

import common.instruction.exceptions.NoSuchInstructionException;
import io.atlassian.fugue.Either;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PartiallyValidInstructionTests {
  void assertIsPartiallyValidInstruction(DecompiledInstruction inst) {
    assertTrue(inst.isPartiallyValid(), "Expected a partially valid instruction. Got: " + inst);
  }

  void assertIsPartiallyValidInstruction(long machineCode) throws NoSuchInstructionException {
    assertIsPartiallyValidInstruction(DecompiledInstruction.from(machineCode));
  }

  @Test
  @DisplayName("\"iname rd, rs\"-instructions are partially legal when rt!=0")
  void testThatInstructionsOnTheForm_INAME_RD_RS_areInvalidIf_RT_isNotZero()
        throws Exception {
    // JALR is expressed on the form "iname rd, rs".
    long numberWithNonZeroRT = Instruction.JALR.getNumericRepresentation();
    numberWithNonZeroRT |= (4 << 16); // Add 4 in the "rt" field.
    assertIsPartiallyValidInstruction(numberWithNonZeroRT);
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
    }
  }
}
