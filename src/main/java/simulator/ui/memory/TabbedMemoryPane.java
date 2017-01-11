package simulator.ui.memory;

import javax.swing.*;
import java.awt.*;

public class TabbedMemoryPane extends JTabbedPane {
  public TabbedMemoryPane(InstructionMemoryPanel instructionMemoryPanel,
                          DataMemoryPanel dataMemoryPanel) {
    super();
    this.setBorder(BorderFactory.createTitledBorder("Memory"));
    this.addTab(instructionMemoryPanel.getLabel(), instructionMemoryPanel);
    this.addTab(dataMemoryPanel.getLabel(), dataMemoryPanel);
    this.setPreferredSize(new Dimension(300, 400));
  }
}
