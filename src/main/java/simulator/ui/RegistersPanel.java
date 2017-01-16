package simulator.ui;

import common.annotations.InstantiateOnEDT;
import common.hardware.Register;
import common.hardware.RegisterFile;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.val;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.function.Function;

import static java.lang.String.*;

@InstantiateOnEDT
@Value
class RegistersPanel extends JPanel implements ChangeRadixDisplayCapable {
  RegisterFile rf;
  @NonFinal
  int[] previouslyDisplayedValues;

  JTable table;
  DefaultTableModel tableModel;
  int indexOfValueColumn;
  int indexOfRegColumn = 0;
  int indexOf
  int noOfRows;

  Function<Integer, String> displayAsHex = (i) -> "0x" + Integer.toHexString(i);
  Function<Integer, String> displayAsDec = String::valueOf;

  @NonFinal
  Function<Integer, String> radixDisplayFunc = displayAsHex;

  RegistersPanel(RegisterFile rf) {
    super(new BorderLayout());
    this.rf = rf;

    // We do not want to show the actual column headers, we just define
    // them so that we ourselves can make sense of our code more easily
    Object[] columnNames = new Object[]{"Ri", "[$reg]", "=", "value"};
    int noOfColumns = columnNames.length;

    Register[] registers = rf.getRegisters();
    noOfRows = registers.length;
    indexOfValueColumn = noOfColumns - 1;
    Object[][] data = new Object[noOfRows][noOfColumns];

    for (int i = 0; i < noOfRows; i++) {
      data[i] = greyRow(registers[i], i);
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
    if (radix == Radix.HEX) {
      radixDisplayFunc = displayAsHex;
    } else if (radix == Radix.DECIMAL) {
      radixDisplayFunc = displayAsDec;
    } else {
      String e = "Trying to set the register radix display to: " +
            "\"" + radix.name() + "\""
            + "but there is no code-path for that radix";
      throw new IllegalStateException(e);
    }

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

  private void displayRegisterFile() {
    Register[] registers = rf.getRegisters();

    for (int rowIndex = 0; rowIndex < noOfRows; rowIndex++) {
      Register r = registers[rowIndex];
      val actual = r.getValue();
      val displayedValue = radixDisplayFunc.apply(actual);
      String c1 = "R" + rowIndex;
      String mnemonic = r.getName();
      if (previouslyDisplayedValues[rowIndex] == actual) {
        // setValueAt calls "fireTableCellUpdated" which executes
        // operations on the EDT. Hence, it must be wrapped in an
        // invokeLater call. This clause usually happens when
        // the radix is updated and not the register file, but also
        // when the register file is updated but not on this particular row.
        int finalRowIndex = rowIndex;

        // We only need to re-render the register value (in case the radix display is changed)
        SwingUtilities.invokeLater(() -> {
          tableModel.setValueAt(displayedValue, finalRowIndex, indexOfValueColumn);
        });
      } else {
        // We need to update the whole line
        c1 = html(bold(c1));
        mnemonic = html(bold(mnemonic));
        SwingUtilities.invokeLater(() -> {
          tableModel.setValueAt();
        });
      }
    }
  }

  public void update() {
    displayRegisterFile();
  }
}
