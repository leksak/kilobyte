package simulator.ui.memory;

import common.annotations.InstantiateOnEDT;
import common.annotations.InvokeLaterNotNecessary;
import lombok.extern.java.Log;
import simulator.Memory;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

@InstantiateOnEDT
@Log
public abstract class MemoryPanel<T> extends JPanel {
  DefaultListModel<String> model = new DefaultListModel<>();
  JList<String> displayList = new JList<>(model);
  Memory<T> memory;
  String label;

  public MemoryPanel(Memory<T> memory, String label) {
    super(new BorderLayout());
    this.memory = memory;
    this.label = label;

    JScrollPane scrollPane = new JScrollPane(displayList);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    this.add(scrollPane, BorderLayout.CENTER);

    populateList();
  }

  @InvokeLaterNotNecessary
  public void update() { // Call whenever the underlying memory has changed.
    // Events on the EDT are performed in order, so this is
    // guaranteed to happen before we start adding data back
    // into the model.
    clearList();
    populateList();
  }

  private void clearList() {
    SwingUtilities.invokeLater(() -> model.removeAllElements());
  }

  private void populateList() {
    log.info("Populating memory: " + label);
    T[] memoryContents = memory.getMemoryContents();
    for (int i = 0; i < memoryContents.length; i++) {
      if (memoryContents[i] == null) {
        log.warning("Found null entry in memory at index=" + i);
        break;
      }
      model.addElement(memoryContents[i].toString());
    }
  }

  public String getLabel() {
    return label;
  }
}
