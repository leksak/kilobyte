package simulator.ui;

import common.instruction.Instruction;

import javax.swing.*;
import java.awt.*;

public class InstructionRenderer extends JLabel implements ListCellRenderer<Instruction> {
  @Override
  public Component getListCellRendererComponent(
        JList<? extends Instruction> jList,
        Instruction instruction, int index,
        boolean isSelected, boolean hasFocus) {
    setText(instruction.getMnemonicRepresentation());
    return this;
  }
}
