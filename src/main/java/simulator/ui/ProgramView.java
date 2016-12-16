package simulator.ui;

import common.annotations.InstantiateOnEDT;
import common.annotations.InvokeLaterNotNecessary;
import common.instruction.Instruction;
import lombok.EqualsAndHashCode;
import lombok.Value;
import simulator.program.Program;

import javax.swing.*;

@InstantiateOnEDT
@Value
@EqualsAndHashCode(callSuper = true)
class ProgramView extends JPanel {
  DefaultListModel<Instruction> programModel = new DefaultListModel<>();

  JList<Instruction> programFrontend = new JList<>();

  public ProgramView() {
    super();
    programFrontend.setModel(programModel);
    this.add(programFrontend);
  }

  @InvokeLaterNotNecessary
  public void display(Program p) {
    SwingUtilities.invokeLater(() -> {
      programModel.clear(); // Clear the old instructions - if any

      // Adding the elements has to happen on the
      p.getInstructions().forEach(programModel::addElement);
    });
  }
}
