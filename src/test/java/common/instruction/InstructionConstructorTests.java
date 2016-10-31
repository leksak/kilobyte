package common.instruction;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InstructionConstructorTests {
  @Test
  void gettingTheAddPrototype() {
    Instruction actual = Instruction.getPrototype("add");
    assertEquals(Instruction.ADD, actual);
    //A a = new A("Hi");
    //a.printAllStatics();
  }

  @Test
  void gettingTheNOPInstructionFromAllZeroes() throws NoSuchInstructionException {
    //Instruction actual = Instruction.unsafeFrom(0);
    //assertEquals(Instruction.NOP, actual);
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
