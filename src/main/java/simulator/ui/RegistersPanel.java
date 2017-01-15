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

@InstantiateOnEDT
@Value
class RegistersPanel extends JPanel implements ChangeRadixDisplayCapable {
  RegisterFile rf;
  JTable table;
  DefaultTableModel tableModel;
  int indexOfValueColumn;
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
      String c1 = "R" + i; // column 1

      Register r = registers[i];
      String mnemonic = "[" + r.getName() + "]";
      String value = String.valueOf(r.getValue());
      data[i] = new Object[]{ c1, mnemonic, "=", value };
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

  private void displayRegisterFile() {
    Register[] registers = rf.getRegisters();
    for (int rowIndex = 0; rowIndex < noOfRows; rowIndex++) {
      val actual = registers[rowIndex].getValue();
      val displayedValue = radixDisplayFunc.apply(actual);

      // setValueAt calls "fireTableCellUpdated" which executes
      // operations on the EDT. Hence, it must be wrapped in an
      // invokeLater call
      int finalRowIndex = rowIndex;
      SwingUtilities.invokeLater(() -> {
        tableModel.setValueAt(displayedValue, finalRowIndex, indexOfValueColumn);
      });
    }
  }

  public void update() {
    displayRegisterFile();
  }
}
