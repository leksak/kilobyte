package common.instruction;

import common.instruction.exceptions.NoSuchInstructionException;
import io.atlassian.fugue.Either;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.expectThrows;

class InstructionConstructorTests {
  @Test
  void nonSymbolicNamesAreHandledAsWell() throws Exception {
    Instruction actual = Instruction.from("add $9, $10, $11");
    Instruction expected = Instruction.from("add $t1, $t2, $t3");

    assertEquals(expected, actual);
  }

  /*
  @Test
  void gettingTheNOPInstructionFromAllZeroes() throws NoSuchInstructionException {
    Instruction actual = Instruction.unsafeFrom(0);
    assertEquals(Instruction.NOP, actual);
  }
*/
  @Test
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
  void accessingTheAddPrototype() throws Exception {
    Instruction variable = Instruction.from("add $t1, $t2, $t3");
    assertEquals(Instruction.ADD, variable);

    Instruction symbolic = Instruction.from("add $9, $10, $11");
    assertEquals(variable, symbolic);

    Instruction numeric = Instruction.unsafeFrom(0x014b4820);
    assertEquals(symbolic, numeric);

  }

  @Test
  void accessingTheSubPrototype() throws Exception {
    Instruction variable = Instruction.from("sub $t1, $t2, $t3");
    assertEquals(Instruction.SUB, variable);

    Instruction symbolic = Instruction.from("sub $9, $10, $11");
    assertEquals(variable, symbolic);

    Instruction instructionNumeric = Instruction.unsafeFrom(0x014b4822);
    assertEquals(variable, instructionNumeric);
  }

  @Test
  void testInameRdRs() throws Exception {
    Instruction mnemonic = Instruction.from("jalr $t1, $t2");
    assertEquals(Instruction.JALR, mnemonic);

    Instruction instructionNumeric = Instruction.unsafeFrom(0x01404809);
    assertEquals(Instruction.JALR, instructionNumeric);
    assertEquals(mnemonic, instructionNumeric);
  }

  @Test
  void testInameRdRsYieldsAPartiallyValidInstructionWhenRtIsNonZero() throws Exception {
    Either<Instruction, PartiallyValidInstruction> instructionNumeric = Instruction.from(0x01404809 | (4 << 16));

    assertTrue(instructionNumeric.isRight());
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
  void testNoSuchInstructionExceptionIsThrownOnUnknown32BitInteger() {
    expectThrows(NoSuchInstructionException.class, () -> Instruction.unsafeFrom(0xFFFFFF));
  }
}
