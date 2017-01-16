package simulator.ui;

import common.annotations.CallOnEDT;
import common.annotations.InstantiateOnEDT;
import common.annotations.InvokeLaterNotNecessary;
import common.instruction.Instruction;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.NonFinal;
import simulator.program.Program;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@InstantiateOnEDT
@Value
@EqualsAndHashCode(callSuper = true)
class ProgramView extends JPanel {
  int INITIAL_NO_OF_ROWS = 0;
  int NO_OF_COLUMNS = 2;
  int INSTRUCTION_POINTER_COL_INDEX = 0;

  @NonFinal
  int currentRowIndex = 0;

  List<Instruction> instructionsInTable = new ArrayList<>();
  DefaultTableModel tableModel = new DefaultTableModel(INITIAL_NO_OF_ROWS, NO_OF_COLUMNS) {
    @Override
    public boolean isCellEditable(int row, int column) {
      // Makes all cells uneditable
      return false;
    }
  };

  ImageIcon currentInstructionPointer = Icon.getIcon(Toolkit.getDefaultToolkit(), this.getClass(), Icon.Name.INSTRUCTION_POINTER);

  JTable table = new JTable(tableModel) {
      //  Returning the Class of each column will allow different
      //  renderers to be used based on Class
      public Class getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
  };

  public ProgramView() {
    // The BorderLayout is what allows us to fit the text pane to the panel
    super(new BorderLayout());
    this.setBorder(BorderFactory.createTitledBorder("Program"));
    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    this.add(scrollPane, BorderLayout.CENTER);
    table.setShowGrid(false);
    table.setTableHeader(null);

    // The A is the default column name
    table.getColumn("A").setPreferredWidth(18);
    table.getColumn("A").setMaxWidth(18);
    table.getColumn("A").setMinWidth(18);

    append("No program is loaded: Try ALT+F by CTRL+L to open the file browser, or use the \"File\" menu in the top left corner");
    highlightLine(0);
  }

  @InvokeLaterNotNecessary
  public void display(Program p) {
    SwingUtilities.invokeLater(() -> {
      // Clear the old instructions  if any
      tableModel.getDataVector().removeAllElements();
      instructionsInTable.clear();

      // notifies the JTable that the model has changed
      tableModel.fireTableDataChanged();

      // Adding the elements has to happen on the EDT
      p.getInstructions().forEach(this::append);
      highlightLine(0);
    });
  }

  private void append(String s) {
    tableModel.addRow(new Object[]{null, s});
  }

  @InvokeLaterNotNecessary
  public void highlightLine(int rowIndex) {
    tableModel.setValueAt(new EmptyIcon(16, 16), currentRowIndex, INSTRUCTION_POINTER_COL_INDEX);
    tableModel.setValueAt(currentInstructionPointer, rowIndex, INSTRUCTION_POINTER_COL_INDEX);
    currentRowIndex = rowIndex;
  }

  public void reset() {
    SwingUtilities.invokeLater(() -> {
      highlightLine(0);
    });
  }

  private void append(Instruction i) {
    instructionsInTable.add(i);
    append(i.getMnemonicRepresentation());
  }
}
