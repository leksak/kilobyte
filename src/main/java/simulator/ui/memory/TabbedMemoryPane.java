package simulator.ui.memory;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class TabbedMemoryPane extends JTabbedPane {
  public TabbedMemoryPane(InstructionMemoryPanel imp, DataMemoryPanel dmp) {
    super();
    this.setBorder(BorderFactory.createTitledBorder("Memory"));
    add(imp, "Instruction");
    add(dmp, "Data");
    this.setPreferredSize(new Dimension(400, 400));
  }
}
