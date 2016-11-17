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
    @Test
    @DisplayName("\"iname rd, rs\"-instructions are partially legal when rt!=0")
    void testThatInstructionsOnTheForm_INAME_RD_RS_areInvalidIf_RT_isNotZero()
          throws Exception {
      // JALR is expressed on the form "iname rd, rs".
      long numberWithNonZeroRT = Instruction.JALR.getNumericRepresentation();
      numberWithNonZeroRT |= (4 << 16);
      Either<Instruction, PartiallyValidInstruction> instructionNumeric =
            Instruction.from(numberWithNonZeroRT);
      assertTrue(instructionNumeric.isRight());
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
