package simulator.ui.memory;

import common.annotations.InstantiateOnEDT;
import lombok.extern.java.Log;
import simulator.DataMemory;

@InstantiateOnEDT
@Log
public class DataMemoryPanel extends MemoryPanel {
  public DataMemoryPanel(DataMemory memory) {
    super(memory, "Data");
  }

  @Override
  public void populateList() {
    log.info("Updating Data Memory display");
    super.populateList();
  }
}
