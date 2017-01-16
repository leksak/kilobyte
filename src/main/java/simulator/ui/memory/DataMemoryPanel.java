package simulator.ui.memory;

import common.annotations.InstantiateOnEDT;
import common.machinecode.OperationsKt;
import lombok.Value;
import lombok.experimental.NonFinal;
import simulator.DataMemory;
import simulator.Memory;
import simulator.ui.ChangeRadixDisplayCapable;
import simulator.ui.Radix;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

@InstantiateOnEDT
@Value
public class DataMemoryPanel extends JPanel implements ChangeRadixDisplayCapable {
  DefaultTableModel dtm;
  JTable table;
  DataMemory memory;

  Object[] columnNames = new Object[]{
        "Addr", "[31:0]", "[31:24]", "[23:16]", "[15:8]", "[7:0]"};
  int ADDRESS_COLUMN = 0;
  int _31to0_COLUMN = 1;
  int _31to24_COLUMN = 2;
  int _23to16_COLUMN = 3;
  int _15to8_COLUMN = 4;
  int _7to0_COLUMN = 5;

  @NonFinal
  Radix currentRadix = Radix.HEX;

  public DataMemoryPanel(DataMemory memory) {
    super(new BorderLayout());
    this.memory = memory;

    int noOfRows = memory.getNO_OF_BYTES()/4;
    int noOfColumns = columnNames.length;
    Object[][] data = new Object[noOfRows][noOfColumns];

    dtm = new DefaultTableModel(data, columnNames);
    table = new JTable(dtm);

    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    this.add(scrollPane, BorderLayout.CENTER);
    update();
  }

  String prettify(byte b) {
    if (currentRadix == Radix.HEX) {
      return "0x" + Integer.toHexString(b);
    }
    return String.valueOf(b);
  }

  String prettify(int i) {
    if (currentRadix == Radix.HEX) {
      return "0x" + Integer.toHexString(i);
    }
    return String.valueOf(i);
  }

  public void update() {
    for (int i = 0; i < memory.getNO_OF_BYTES()/4; i++) {
      int rowIndex = i;
      SwingUtilities.invokeLater(() -> {
        int address = rowIndex * 4;
        int _32bitWord = memory.readWordFrom(address);
        byte lowestByte = OperationsKt.nthByte(_32bitWord, 0);
        byte byte2 = OperationsKt.nthByte(_32bitWord, 1);
        byte byte3 = OperationsKt.nthByte(_32bitWord, 2);
        byte highestByte = OperationsKt.nthByte(_32bitWord, 3);

        dtm.setValueAt(address, rowIndex, ADDRESS_COLUMN);
        dtm.setValueAt(prettify(_32bitWord), rowIndex, _31to0_COLUMN);
        dtm.setValueAt(prettify(highestByte), rowIndex, _31to24_COLUMN);
        dtm.setValueAt(prettify(byte3), rowIndex, _23to16_COLUMN);
        dtm.setValueAt(prettify(byte2), rowIndex, _15to8_COLUMN);
        dtm.setValueAt(prettify(lowestByte), rowIndex, _7to0_COLUMN);
      });
    }
  }

  @Override
  public void setRadix(Radix r) {
    currentRadix = r;
    update();
  }
}
