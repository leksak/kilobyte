package simulator.ui;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import common.annotations.InstantiateOnEDT;
import common.hardware.Register;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.jcip.annotations.NotThreadSafe;
import simulator.Observable;
import simulator.Observer;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@InstantiateOnEDT
@NotThreadSafe
@Value
@EqualsAndHashCode(callSuper = true)
public class RegisterMenu extends JMenu implements Observable<RegisterMenu> {
  List<Observer<RegisterMenu>> observers = new ArrayList<>();
  JRadioButtonMenuItem hex = new JRadioButtonMenuItem("Hex", true);
  JRadioButtonMenuItem decimal = new JRadioButtonMenuItem("Decimal");
  ButtonGroup buttonGroup = ButtonGroupFactory.from(hex, decimal);

  RegisterMenu() {
    super("Registers");
    add(hex);
    add(decimal);
  }

  @Override
  public void addObserver(Observer<RegisterMenu> o) {
    observers.add(o);
  }

  @Override
  public ImmutableCollection<Observer<RegisterMenu>> observers() {
    return ImmutableList.copyOf(observers);
  }

  @Override
  public RegisterMenu reify() {
    return this;
  }
}
