package simulator.ui.memory;

import common.annotations.InstantiateOnEDT;
import lombok.extern.java.Log;
import simulator.InstructionMemory;

@InstantiateOnEDT
@Log
public class InstructionMemoryPanel extends MemoryPanel {
  public InstructionMemoryPanel(InstructionMemory memory) {
    super(memory, "Instruction");
  }

  @Override
  public void populateList() {
    log.info("Updating the Instruction Memory display");
    super.populateList();
  }
}
