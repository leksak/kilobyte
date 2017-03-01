package kilobyte.simulator.ui.memory;

import kilobyte.common.annotations.InstantiateOnEDT;
import kilobyte.common.annotations.InvokeLaterNotNecessary;
import lombok.extern.java.Log;
import kilobyte.simulator.hardware.InstructionMemory;
import kilobyte.simulator.ui.ChangeRadixDisplayCapable;
import kilobyte.simulator.ui.utils.Radix;

import javax.swing.*;
import java.awt.*;

import static java.lang.String.format;

@InstantiateOnEDT
@Log
public class InstructionMemoryPanel extends JPanel implements ChangeRadixDisplayCapable {
  DefaultListModel<String> model = new DefaultListModel<>();
  JList<String> displayList = new JList<>(model);
  InstructionMemory instructionMemory;
  String label;
  Radix currentRadix = Radix.HEX;

  public InstructionMemoryPanel(InstructionMemory instructionMemory, String label) {
    super(new BorderLayout());
    this.instructionMemory = instructionMemory;
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

  public void populateList() {
    String[] memoryContents;

    if (currentRadix == Radix.HEX) {
      memoryContents = instructionMemory.toHexStringArray();
    } else {
      memoryContents = instructionMemory.toDecimalStringArray();
    }

    int noOfEntries = memoryContents.length;
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

  public void display(InstructionMemory instructionMemory) {
    this.instructionMemory = instructionMemory;
    update();
  }
}
