package simulator.ui.memory;

import common.annotations.InstantiateOnEDT;
import common.annotations.InvokeLaterNotNecessary;
import lombok.extern.java.Log;
import simulator.Memory;
import simulator.ui.ChangeRadixDisplayCapable;
import simulator.ui.Radix;

import javax.swing.*;
import java.awt.*;

import static java.lang.String.format;

@InstantiateOnEDT
@Log
public abstract class MemoryPanel extends JPanel implements ChangeRadixDisplayCapable {
  DefaultListModel<String> model = new DefaultListModel<>();
  JList<String> displayList = new JList<>(model);
  Memory memory;
  String label;
  Radix currentRadix = Radix.HEX;

  public MemoryPanel(Memory memory, String label) {
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
    log.info("Updating: " + label);
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
    String[] memoryContents;

    if (currentRadix == Radix.HEX) {
      memoryContents = memory.toHexStringArray();
    } else {
      memoryContents = memory.toDecimalStringArray();
    }

    int noOfEntries = memoryContents.length;
    log.info(format("Populating memory: %s with %d entries", label, noOfEntries));
    for (int i = 0; i < noOfEntries; i++) {
      String entry = memoryContents[i];
      if (entry == null) {
        log.warning("Found null entry in memory at index=" + i);
        break;
      }

      // invokeLater call is necessary
      SwingUtilities.invokeLater(() -> model.addElement(entry));
    }
  }

  @Override
  public void setRadix(Radix r) {
    currentRadix = r;
    clearList();
    populateList();
  }


  public String getLabel() {
    return label;
  }
}
