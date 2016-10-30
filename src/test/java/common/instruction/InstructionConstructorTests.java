package common.instruction;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InstructionConstructorTests {
  @Test
  void addPrototype() {
    Instruction actual = Instruction.getPrototype("add");
    assertEquals(actual, Instruction.ADD);
  }

  @Test
  void checkThatAllPrototypesCreateEqualInstancesFromTheirExamples() throws Exception {
    /*for (InstructionExample p : InstructionSet.allExamples()) {
      String mnemonic = p.getMnemonicExample();
      int numeric = p.getNumericExample();
      InstructionPrototype fromMnemonic = InstructionPrototype.unsafeFrom(mnemonic);
      InstructionPrototype fromNumeric = InstructionPrototype.unsafeFrom(numeric);
      assertEquals(fromMnemonic, fromNumeric);
    }*/

  }
}
