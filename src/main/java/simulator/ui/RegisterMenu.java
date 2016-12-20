package simulator.ui;

import common.annotations.InstantiateOnEDT;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.NonFinal;
import net.jcip.annotations.NotThreadSafe;
import simulator.Observable;
import simulator.Observer;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

@InstantiateOnEDT
@NotThreadSafe
@Value
@EqualsAndHashCode(callSuper = true)
// Decides the radix used in the registers panel
public class RegisterMenu extends JMenu implements Observable<RegisterMenu> {
  // Lombok will generate our getObservers method
  List<Observer<RegisterMenu>> observers = new ArrayList<>();
  JRadioButtonMenuItem hex = new JRadioButtonMenuItem("Hex", true);
  JRadioButtonMenuItem decimal = new JRadioButtonMenuItem("Decimal");
  ButtonGroup buttonGroup = ButtonGroupFactory.from(hex, decimal);

  @NonFinal
  Radix radix = Radix.HEX;

  RegisterMenu() {
    super("Registers");
    add(hex);
    add(decimal);

    hex.addActionListener(e -> {
      // We only want to perform this action when the radix changes
      if (this.radix != Radix.HEX) {
        this.radix = Radix.HEX;
        notifyObservers();
      }
    });

    decimal.addActionListener(e -> {
      // We only want to perform this action when the radix changes
      if (this.radix != Radix.DECIMAL) {
        this.radix = Radix.DECIMAL;
        notifyObservers();
      }
    });
  }

  @Override
  public RegisterMenu reify() {
    return this;
  }
}
