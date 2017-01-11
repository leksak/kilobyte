package simulator.ui;

import common.annotations.InstantiateOnEDT;
import common.annotations.InvokeLaterNotNecessary;
import lombok.Value;

import javax.swing.*;
import java.awt.*;

@InstantiateOnEDT
@Value
public class ProgramCounterView extends JPanel {
  JLabel label = new JLabel();
  public ProgramCounterView() {
    super(new FlowLayout(FlowLayout.LEFT));
    add(label);
    setText("PC: 0");
  }

  @InvokeLaterNotNecessary
  public void setText(String s) {
    SwingUtilities.invokeLater(() -> label.setText(s));
  }
}
