package common.instruction;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InstructionConstructorTests {
  @Test
  void accessingTheAddPrototype() throws Exception {
    Instruction actual = Instruction.from("add $t1, $t2, $t3");

    assertEquals(Instruction.ADD, actual);
  }

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
    /*for (InstructionExample p : InstructionSet.allExamples()) {
      String mnemonic = p.getMnemonicExample();
      int numeric = p.getNumericExample();
      InstructionPrototype fromMnemonic = InstructionPrototype.unsafeFrom(mnemonic);
      InstructionPrototype fromNumeric = InstructionPrototype.unsafeFrom(numeric);
      assertEquals(fromMnemonic, fromNumeric);
    }*/

  }
}
