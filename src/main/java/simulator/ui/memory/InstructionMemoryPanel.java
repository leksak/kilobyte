package simulator.ui.memory;

import common.annotations.InstantiateOnEDT;
import simulator.InstructionMemory;

@InstantiateOnEDT
public class InstructionMemoryPanel extends MemoryPanel<Long> {
  public InstructionMemoryPanel(InstructionMemory memory) {
    super(memory, "Instruction Memory");
  }
}
