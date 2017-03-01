package kilobyte.simulator.ui;

import kilobyte.common.annotations.InstantiateOnEDT;
import kilobyte.common.annotations.InvokeLaterNotNecessary;
import kilobyte.simulator.hardware.ProgramCounter;

import javax.swing.*;
import java.awt.*;

@InstantiateOnEDT
class ProgramCounterView extends JPanel {
  JLabel label = new JLabel();
  ProgramCounter programCounter;
  public ProgramCounterView(ProgramCounter programCounter) {
    super(new FlowLayout(FlowLayout.LEFT));
    this.programCounter = programCounter;
    add(label);
    setText("PC: 0");
  }

  @InvokeLaterNotNecessary
  public void setText(String s) {
    SwingUtilities.invokeLater(() -> label.setText(s));
  }

  public void update() {
    setText("PC: " + programCounter.getAddressPointer());
  }

  public void display(ProgramCounter programCounter) {
    this.programCounter = programCounter;
    update();
  }
}
