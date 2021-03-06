package kilobyte.simulator.ui.menu;

import kilobyte.common.annotations.InstantiateOnEDT;
import kilobyte.simulator.ui.ChangeRadixDisplayCapable;
import kilobyte.simulator.ui.utils.ButtonGroupFactory;
import kilobyte.simulator.ui.utils.Radix;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.NonFinal;

import javax.swing.*;

/**
 * Toggles between Radix.HEX and Radix.DEC on a RadixToggle-able
 * JPanel
 */
@InstantiateOnEDT
@Value
@EqualsAndHashCode(callSuper = true)
public class RadixMenu extends JMenu {
  JRadioButtonMenuItem hex = new JRadioButtonMenuItem("Hex", true);
  JRadioButtonMenuItem decimal = new JRadioButtonMenuItem("Decimal");
  ButtonGroup buttonGroup = ButtonGroupFactory.from(hex, decimal);

  @NonFinal
  Radix radix = Radix.HEX; // Default setting

  public RadixMenu(String name, ChangeRadixDisplayCapable r) {
    super(name);
    add(hex);
    add(decimal);

    // Ensure that the view and this controller defaults to the same
    // radix.
    r.setRadix(radix);

    hex.addActionListener(e -> {
      // We only want to perform this action when the radix changes
      if (this.radix != Radix.HEX) {
        this.radix = Radix.HEX;
        r.setRadix(radix);
      }
    });

    decimal.addActionListener(e -> {
      // We only want to perform this action when the radix changes
      if (this.radix != Radix.DECIMAL) {
        this.radix = Radix.DECIMAL;
        r.setRadix(radix);
      }
    });
  }
}
