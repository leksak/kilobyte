package simulator.ui;

import common.annotations.InstantiateOnEDT;
import common.annotations.InvokeLaterNotNecessary;
import common.instruction.Instruction;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.NonFinal;
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
    this.setBorder(BorderFactory.createTitledBorder("Program"));
    JScrollPane scrollPane = new JScrollPane(programFrontend);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    this.add(scrollPane, BorderLayout.CENTER);
    programFrontend.setEditable(false);
    append("No MIPS32 program has been loaded into memory.");
    append("Try ALT+F by CTRL+L to open the file browser, or use the");
    append("\"File\"-menu in the top-left corner.");

  }

  @InvokeLaterNotNecessary
  public void display(Program p) {
    SwingUtilities.invokeLater(() -> {
      // Clear the old instructions - if any
      programFrontend.setText("");

      // Adding the elements has to happen on the EDT
      p.getInstructions().forEach(this::append);
    });
  }

  @NonFinal
  boolean displayingProgram = false;

  private void append(String s) {
    try {
      programDocument.insertString(programDocument.getLength(), s + "\n", null);
    } catch (BadLocationException e) {
      e.printStackTrace();
    }
  }

  private void append(Instruction i) {
    if (!displayingProgram) {
      displayingProgram = true;
    }

    append(i.getMnemonicRepresentation());
    instructionsInDisplayedProgram.add(i);
  }
}
