package simulator.ui;

import common.annotations.InstantiateOnEDT;
import common.instruction.Instruction;
import simulator.Simulator;

import javax.swing.*;
import java.util.List;
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
