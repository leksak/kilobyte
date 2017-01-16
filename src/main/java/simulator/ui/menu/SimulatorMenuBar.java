package simulator.ui.menu;

import common.annotations.InstantiateOnEDT;

import javax.swing.*;
import java.util.Arrays;

@InstantiateOnEDT
public class SimulatorMenuBar extends JMenuBar {
  public SimulatorMenuBar(JMenuItem... menuItems) {
    super();
    Arrays.stream(menuItems).forEach(this::add);

  }
}
