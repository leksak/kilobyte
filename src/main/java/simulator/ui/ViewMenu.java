package simulator.ui;

import common.annotations.InstantiateOnEDT;
import lombok.val;
import simulator.ui.memory.MemoryPanel;

import javax.swing.*;

import static java.awt.event.KeyEvent.VK_R;

/**
 * The {@code ViewMenu} contains a set of menu items that affect
 * the display settings for different aspects of the application.
 */
@InstantiateOnEDT
class ViewMenu extends JMenu {
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
  ViewMenu(RegistersPanel rp, MemoryPanel imp, MemoryPanel dmp) {
    super("View");
    val registerRadixMenu = new RadixMenu("Registers", rp);
    val instructionMemoryMenu = new RadixMenu("Instruction Memory", imp);
    val dataMemoryMenu = new RadixMenu("Data Memory", dmp);

    add(registerRadixMenu);
    add(instructionMemoryMenu);
    add(dataMemoryMenu);
    registerRadixMenu.setMnemonic(VK_R);
  }
}
