package simulator.ui;

import common.annotations.InstantiateOnEDT;

import javax.swing.*;
import java.awt.*;

@InstantiateOnEDT
public class SimulatorMenuBar extends JMenuBar {
  SimulatorMenuBar(FileMenu fileMenu, RegisterMenu registerMenu) {
    super();

    Toolkit tk = Toolkit.getDefaultToolkit();
    //Class<SimulatorMenuBar> thisClass = this.getClass();
    JMenuItem run = new JMenuItem(); // TODO: Add run action
    run.setIcon(Icon.play(tk, this.getClass()));
    run.setToolTipText("Start the simulator");

    add(fileMenu);
    add(registerMenu);
    add(run);
  }
}
