package common.instruction;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InstructionConstructorTests {
  @Test
  void checkThatAllPrototypesCreateEqualInstancesFromTheirExamples() throws Exception {
    for (InstructionExample p : InstructionSet.allExamples()) {
      String mnemonic = p.getMnemonicExample();
      int numeric = p.getNumericExample();
      Instruction fromMnemonic = Instruction.unsafeFrom(mnemonic);
      Instruction fromNumeric = Instruction.unsafeFrom(numeric);
      assertEquals(fromMnemonic, fromNumeric);
    }
  }
}
