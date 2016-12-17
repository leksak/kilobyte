package simulator.ui;

import common.annotations.InstantiateOnEDT;
import common.hardware.Register;
import common.hardware.RegisterFile;
import lombok.Value;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

@InstantiateOnEDT
@Value
public class RegistersPanel extends JPanel {
  RegisterFile rf;
  JTable table;
  DefaultTableModel tableModel;

  public RegistersPanel(RegisterFile rf) {
    this.rf = rf;

    // We do not want to show the actual column headers, we just define
    // them so that we ourselves can make sense of our code more easily
    Object[] columnNames = new Object[]{"Ri", "[$reg]", "=", "value"};

    Object[][] data = new Object[32][4];
    Register[] registers = rf.getRegisters();

    for (int i = 0; i < registers.length; i++) {
      String c1 = "R" + i; // column 1

      Register r = registers[i];
      String mnemonic = "[" + r.getName() + "]";
      String value = String.valueOf(r.getValue());
      data[i] = new Object[]{ c1, mnemonic, "=", value };
    }

    tableModel = new DefaultTableModel(data, columnNames);
    table = new JTable(tableModel);
    table.setShowGrid(false);

    add(table);
  }
}
