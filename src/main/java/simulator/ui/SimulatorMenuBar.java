package simulator.ui;

import common.annotations.InstantiateOnEDT;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

@InstantiateOnEDT
public class SimulatorMenuBar extends JMenuBar {
  SimulatorMenuBar(JMenuItem... menuItems) {
    super();

    Toolkit tk = Toolkit.getDefaultToolkit();
    //Class<SimulatorMenuBar> thisClass = this.getClass();
    JMenuItem run = new JMenuItem(); // TODO: Add run action
    run.setIcon(Icon.play(tk, this.getClass()));
    run.setToolTipText("Start the simulator");

    Arrays.stream(menuItems).forEach(this::add);

    add(run);
  }
}
