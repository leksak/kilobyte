package simulator.ui;

import common.annotations.InstantiateOnEDT;
import common.annotations.InvokeLaterNotNecessary;
import common.instruction.Instruction;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.val;
import simulator.program.Program;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@InstantiateOnEDT
@Value
@EqualsAndHashCode(callSuper = true)
class ProgramView extends JPanel {
  // Displays the entire program
  JTextPane programFrontend = new JTextPane();
  StyledDocument programDocument = programFrontend.getStyledDocument();

  // Contains all the instructions shown in the programFrontend
  List<Instruction> instructionsInDisplayedProgram = new ArrayList<>();

  public ProgramView() {
    // The BorderLayout is what allows us to fit the text pane to the panel
    super(new BorderLayout());
    JScrollPane scrollPane = new JScrollPane(programFrontend);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    this.add(scrollPane, BorderLayout.CENTER);
    programFrontend.setEditable(false);
  }

  @InvokeLaterNotNecessary
  public void display(Program p) {
    SwingUtilities.invokeLater(() -> {
      // Clear the old instructions - if any
      programFrontend.setText("");

      // Adding the elements has to happen on the
      p.getInstructions().forEach(this::append);
    });
  }

  private void append(Instruction i) {
    try {
      programDocument.insertString(
            programDocument.getLength(),
            "\n" + i.getMnemonicRepresentation(), null);
      instructionsInDisplayedProgram.add(i);
    } catch (BadLocationException e) {
      e.printStackTrace();
    }
  }
}
