package simulator;

import common.instruction.Instruction;

import java.util.ArrayList;
import java.util.List;

public class InstructionMemory {
  private final List<Instruction> instructions = new ArrayList<>();

  public Instruction fromAddress(int address) {
    throw new UnsupportedOperationException();
  }

  public void put(Instruction i) {
    instructions.add(i);
  }

  public void putAll(List<Instruction> instructions) {
    this.instructions.addAll(instructions);
  }
}
