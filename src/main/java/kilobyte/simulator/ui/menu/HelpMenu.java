package kilobyte.simulator.ui.menu;

import kilobyte.common.annotations.InstantiateOnEDT;
import kilobyte.common.instruction.Instruction;
import kilobyte.simulator.Simulator;

import javax.swing.*;
import java.util.Set;
import java.util.StringJoiner;

@InstantiateOnEDT
public class HelpMenu extends JMenu {
  public HelpMenu(JFrame frame) {
    super("Help");
    JMenuItem menuItem = new JMenuItem("Supported Instructions");
    Set<Instruction> supportedInstructions = Simulator.getSupportedInstructions();

    StringJoiner sj = new StringJoiner("\n");
    supportedInstructions.forEach(i -> sj.add(i.getIname()));

    menuItem.addActionListener(event -> {
      JOptionPane.showMessageDialog(frame, sj.toString());
    });

    this.add(menuItem);
  }
}
