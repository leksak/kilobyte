package simulator.ui;

import common.annotations.InstantiateOnEDT;
import common.annotations.InvokeLaterNotNecessary;
import lombok.Value;
import simulator.hardware.PC;

import javax.swing.*;
import java.awt.*;

@InstantiateOnEDT
@Value
public class ProgramCounterView extends JPanel {
  JLabel label = new JLabel();
  PC programCounter;
  public ProgramCounterView(PC programCounter) {
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
}
