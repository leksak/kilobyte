package simulator.ui;

import com.google.common.collect.ImmutableList;
import common.annotations.CallOnEDT;
import common.annotations.InstantiateOnEDT;
import common.instruction.Instruction;
import lombok.EqualsAndHashCode;
import lombok.Value;
import simulator.program.Program;

import javax.swing.*;
import java.util.List;

@InstantiateOnEDT
@Value
class ProgramView extends JPanel {
  DefaultListModel<Instruction> programModel = new DefaultListModel<>();
  JList<Instruction> programFrontend = new JList<>();

  public ProgramView() {
    super();
    programFrontend.setModel(programModel);
  }

  // Should only be called once per program
  @CallOnEDT
  public void display(Program p) {
    programModel.clear(); // Clear the old instructions - if any
    p.getInstructions().forEach(programModel::addElement);
  }
}
