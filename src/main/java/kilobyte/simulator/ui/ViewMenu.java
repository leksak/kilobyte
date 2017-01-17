package kilobyte.simulator.ui;

import kilobyte.common.annotations.InstantiateOnEDT;
import lombok.val;
import kilobyte.simulator.ui.memory.DataMemoryPanel;
import kilobyte.simulator.ui.memory.InstructionMemoryPanel;
import kilobyte.simulator.ui.menu.RadixMenu;

import javax.swing.*;

import static java.awt.event.KeyEvent.VK_R;

/**
 * The {@code ViewMenu} contains a set of menu items that affect
 * the display settings for different aspects of the application.
 */
@InstantiateOnEDT
public class ViewMenu extends JMenu {
  /**
   * To perform its responsibilities it requires that its constituent
   * entries (menu-items) all have a reference to the aspect of
   * the application that it controls, so that they (i.e. the menu
   * items themselves) can communicate user events to the object(s)
   * that are affected by each respective setting. Hence, the
   * dependency injection.
   *
   * @param rp the {@see RegisterPanel} to wit we can change the
   *           radix display settings of.
   */
  ViewMenu(RegistersPanel rp, InstructionMemoryPanel imp, DataMemoryPanel dmp) {
    super("View");
    val registerRadixMenu = new RadixMenu("Registers", rp);
    val instructionMemoryMenu = new RadixMenu("Instruction Memory", imp);

    add(registerRadixMenu);
    add(instructionMemoryMenu);
    add(new RadixMenu("Data Memory", dmp));
    registerRadixMenu.setMnemonic(VK_R);
  }
}
