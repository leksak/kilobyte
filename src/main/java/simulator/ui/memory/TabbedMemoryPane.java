package simulator.ui.memory;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class TabbedMemoryPane extends JTabbedPane {
  public TabbedMemoryPane(MemoryPanel... memoryPanels) {
    super();
    this.setBorder(BorderFactory.createTitledBorder("Memory"));
    Arrays.stream(memoryPanels).forEach(i -> addTab(i.getLabel(), i));
    this.setPreferredSize(new Dimension(300, 400));
  }
}
