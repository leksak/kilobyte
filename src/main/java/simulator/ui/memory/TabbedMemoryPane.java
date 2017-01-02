package simulator.ui.memory;

import javax.swing.*;

public class TabbedMemoryPane extends JTabbedPane {
  public TabbedMemoryPane(InstructionMemoryPanel instructionMemoryPanel,
                          DataMemoryPanel dataMemoryPanel) {
    super();
    this.addTab(instructionMemoryPanel.getLabel(), instructionMemoryPanel);
    this.addTab(dataMemoryPanel.getLabel(), dataMemoryPanel);
  }
}
