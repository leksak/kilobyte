package simulator.ui;

import common.annotations.InstantiateOnEDT;

import javax.swing.*;
import java.awt.*;

@InstantiateOnEDT
public class SimulatorMenuBar extends JMenuBar {
  SimulatorMenuBar(FileMenu fileMenu) {
    super();

    /*Toolkit tk = Toolkit.getDefaultToolkit();
    Class thisClass = this.getClass();
    JMenuItem run = new JMenuItem(); // TODO: Add run action
    run.setIcon(Icon.play(tk, thisClass));*/
    //run.setIcon(new ImageIcon(tk.getImage(thisClass.getResource(Globals.imagesPath+"New16.png"))));

    add(fileMenu);
    //add(run);
  }
}
