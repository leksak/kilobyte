package simulator.ui.memory;

import common.annotations.InstantiateOnEDT;
import common.machinecode.OperationsKt;
import lombok.Value;
import lombok.experimental.NonFinal;
import simulator.hardware.DataMemory;
import simulator.ui.ChangeRadixDisplayCapable;
import simulator.ui.utils.Radix;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import static java.lang.String.format;

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

  boolean[] valuesThatHaveChanged;
  int[] previousValues;
  int noOfRows;
  public DataMemoryPanel(DataMemory memory) {
    super(new BorderLayout());
    this.memory = memory;

    noOfRows = memory.getNO_OF_BYTES()/4;
    int noOfColumns = columnNames.length;
    Object[][] data = new Object[noOfRows][noOfColumns];
    valuesThatHaveChanged = new boolean[noOfRows];
    previousValues = new int[noOfRows];
    
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


  public void renderBold(int rowIndex, int address, 
                         int _32bitWord, byte lowestByte, 
                         byte byte2, byte byte3, byte highestByte) {
    render(rowIndex, html(bold(prettify(address))),
          html(bold(prettify(_32bitWord))),
          html(bold(prettify(lowestByte))),
          html(bold(prettify(byte2))),
          html(bold(prettify(byte3))),
          html(bold(prettify(highestByte))));
  }

  public void reset() {
    memory.resetMemory();

    for (int i = 0; i < noOfRows; i++) {
      valuesThatHaveChanged[i] = false;
      previousValues[i] = 0;
    }
    update();
  }

  public void render(int rowIndex, String address,
                     String _32bitWord, String lowestByte,
                     String byte2, String byte3, String highestByte) {
    SwingUtilities.invokeLater(() -> {
      dtm.setValueAt(address, rowIndex, ADDRESS_COLUMN);
      dtm.setValueAt(_32bitWord, rowIndex, _31to0_COLUMN);
      dtm.setValueAt(highestByte, rowIndex, _31to24_COLUMN);
      dtm.setValueAt(byte3, rowIndex, _23to16_COLUMN);
      dtm.setValueAt(byte2, rowIndex, _15to8_COLUMN);
      dtm.setValueAt(lowestByte, rowIndex, _7to0_COLUMN);
      dtm.fireTableRowsUpdated(rowIndex, rowIndex);
    });
  }



  public void update() {
    for (int rowIndex = 0; rowIndex < noOfRows; rowIndex++) {
      int address = rowIndex * 4;
      int _32bitWord = memory.readWordFrom(address);
      byte lowestByte = OperationsKt.nthByte(_32bitWord, 0);
      byte byte2 = OperationsKt.nthByte(_32bitWord, 1);
      byte byte3 = OperationsKt.nthByte(_32bitWord, 2);
      byte highestByte = OperationsKt.nthByte(_32bitWord, 3);
      int previousValue = previousValues[rowIndex];

      if (previousValue != _32bitWord) {
        valuesThatHaveChanged[rowIndex] = true;
        previousValues[rowIndex] = _32bitWord;
      }

      // Either the value has changed or we've already changed this one in the past
      if (valuesThatHaveChanged[rowIndex]) {
        renderBold(rowIndex, address, _32bitWord, lowestByte, byte2, byte3, highestByte);
      } else {
        renderGrey(rowIndex, address, _32bitWord, lowestByte, byte2, byte3, highestByte);
      }
    }
  }

  private void renderGrey(int rowIndex, int address, 
                          int _32bitWord, byte lowestByte, 
                          byte byte2, byte byte3, 
                          byte highestByte) {
    render(rowIndex, html(grey(prettify(address))), html(grey(prettify(_32bitWord))), html(grey(prettify(lowestByte))),
          html(grey(prettify(byte2))), html(grey(prettify(byte3))), html(grey(prettify(highestByte))));
  }

  @Override
  public void setRadix(Radix r) {
    currentRadix = r;
    update();
  }
}
