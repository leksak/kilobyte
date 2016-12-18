package simulator.ui;

import common.annotations.InstantiateOnEDT;

import javax.swing.*;

@InstantiateOnEDT
public class ProgramCounterView extends JLabel {
  public ProgramCounterView() {
    super();
    this.setText("PC: 0");
  }
}
