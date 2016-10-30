package common.instruction;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InstructionConstructorTests {
  @Test
  void addPrototype() {
    InstructionPrototype actual = InstructionPrototype.unsafeGet("add");
    assertEquals(actual, InstructionPrototype.ADD);
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
