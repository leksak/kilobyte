package kilobyte.simulator.ui;

import kilobyte.common.annotations.InstantiateOnEDT;
import kilobyte.common.hardware.Register;
import kilobyte.common.hardware.RegisterFile;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.val;
import kilobyte.simulator.ui.utils.Radix;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import static java.lang.String.*;

@InstantiateOnEDT
@Value
class RegistersPanel extends JPanel implements ChangeRadixDisplayCapable {
  RegisterFile rf;

  // Used to determine if a value has changed
  int[] previouslyDisplayedValues = new int[32];

  // Defaults to false values
  boolean[] valuesThatHaveChanged = new boolean[32];

  JTable table;
  DefaultTableModel tableModel;

  // We do not want to show the actual column headers, we just define
  // them so that we ourselves can make sense of our code more easily
  Object[] columnNames = new Object[]{"Ri", "[$reg]", "=", "value"};
  int noOfColumns = columnNames.length;
  int noOfRows;

  int indexOfRegColumn = 0;
  int indexOfMnemonicColumn = 1;
  int indexOfEqualsColumn = 2;
  int indexOfValueColumn = 3;

  @NonFinal
  Radix currentRadix = Radix.HEX;

  RegistersPanel(RegisterFile rf) {
    super(new BorderLayout());
    this.rf = rf;

    Register[] registers = rf.getRegisters();
    noOfRows = registers.length;

    Object[][] data = new Object[noOfRows][noOfColumns];

    for (int i = 0; i < noOfRows; i++) {
      data[i] = greyRow(registers[i], i);
      previouslyDisplayedValues[i] = registers[i].getValue();
    }

    /* We assume that the caller instantiates this class on the EDT,
     * therefore these operations are safe.
     */
    tableModel = new DefaultTableModel(data, columnNames);
    table = new JTable(tableModel);
    table.setShowGrid(false);

    add(table);
  }

  @Override
  public void setRadix(Radix radix) {
    currentRadix = radix;
    displayRegisterFile();
  }

  static String bold(String s) {
    return format("<b>%s</b>", s);
  }

  static String html(String s) {
    return format("<html>%s</html>", s);
  }

  static String color(String s, String hex) {
    return format("<p style=\"color: %s\">%s</p>", hex, s);
  }

  static String grey(String s) {
    return color(s,"#B4B4B4");
  }

  static Object[] greyRow(Register r, int rowIndex) {
    String c1 = html(grey("R" + rowIndex)); // column i
    String mnemonic = html(grey("[" + r.getName() + "]"));
    String value = html(grey(valueOf(r.getValue())));
    return new Object[]{ c1, mnemonic, html(grey("=")), value };
  }

  static String prettify(boolean bold, String s) {
    return bold ? html(bold(s)) : html(grey(s));
  }

  void renderRow(int rowIndex) {
    boolean changed = valuesThatHaveChanged[rowIndex];
    int registerValue = rf.getRegisters()[rowIndex].getValue();
    String c1 = prettify(changed, "R[" + rowIndex + "]");
    String c2 = prettify(changed, "[" + RegisterFile.getMnemonic(rowIndex) + "]");
    String c3 = prettify(changed, "=");
    String displayedValue;
    if (currentRadix == Radix.HEX) {
      displayedValue = "0x" + Integer.toHexString(registerValue);
    } else {
      displayedValue = String.valueOf(registerValue);
    }
    String c4 = prettify(changed, displayedValue);

    // setValueAt calls "fireTableCellUpdated" which executes
    // operations on the EDT. Hence, it must be wrapped in an
    // invokeLater call. This clause usually happens when
    // the radix is updated and not the register file, but also
    // when the register file is updated but not on this particular row.
    SwingUtilities.invokeLater(() -> {
      tableModel.setValueAt(c1, rowIndex, indexOfRegColumn);
      tableModel.setValueAt(c2, rowIndex, indexOfMnemonicColumn);
      tableModel.setValueAt(c3, rowIndex, indexOfEqualsColumn);
      tableModel.setValueAt(c4, rowIndex, indexOfValueColumn);
    });
  }

  private void displayRegisterFile() {
    Register[] registers = rf.getRegisters();

    for (int rowIndex = 0; rowIndex < registers.length; rowIndex++) {
      Register r = registers[rowIndex];
      val actual = r.getValue();

      if (!(previouslyDisplayedValues[rowIndex] == actual)) {
        valuesThatHaveChanged[rowIndex] = true;
        previouslyDisplayedValues[rowIndex] = actual;
      }

      // We need to re-write the entire line just in case it has been bold before
      // this happens when the display has been reset
      renderRow(rowIndex);
    }
  }

  public void update() {
    displayRegisterFile();
  }

  public void reset() {
    Register[] registers = rf.getRegisters();
    for (int i = 0; i < 32; i++) {
      valuesThatHaveChanged[i] = false;
      previouslyDisplayedValues[i] = registers[i].getValue();
    }
    update();
  }
}
