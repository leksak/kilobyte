package simulator.ui;

import common.annotations.InstantiateOnEDT;
import lombok.Value;
import simulator.Control;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;

@InstantiateOnEDT
@Value
public class ControlLinesPanel extends JPanel {
  DefaultTableModel tableModel;
  JTable table;
  Object[] columnNames = new Object[]{"RegDst", "ALUSrc", "MemToReg", "RegWrite", "MemRead", "MemWrite", "Branch", "ALUOp1", "ALUOp2"};
  int noOfColumns = columnNames.length;
  private Control control;

  public ControlLinesPanel(Control control) {
    super(new BorderLayout());
    this.control = control;


    Object[][] data = new Object[1][noOfColumns];
    data[0] = control.asObjectArray();

    /* We assume that the caller instantiates this class on the EDT,
     * therefore these operations are safe.
     */
    tableModel = new DefaultTableModel(data, columnNames);
    table = new JTable(tableModel);

    table.setShowGrid(false);
    add(table.getTableHeader(), BorderLayout.NORTH);
    add(table, BorderLayout.CENTER);
  }

  public void update() {
    Object[][] data = new Object[1][noOfColumns];
    data[0] = control.asObjectArray();
    tableModel.setDataVector(data, columnNames);
  }
}
