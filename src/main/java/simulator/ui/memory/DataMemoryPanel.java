package simulator.ui.memory;

import simulator.DataMemory;

public class DataMemoryPanel extends MemoryPanel<Byte> {
  public DataMemoryPanel(DataMemory memory) {
    super(memory, "Data Memory");
  }
}
