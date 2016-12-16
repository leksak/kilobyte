package simulator.ui;

import javax.swing.*;
import java.util.Arrays;

public class ButtonGroupFactory {
  private ButtonGroupFactory() { }

  public static ButtonGroup from(AbstractButton... buttons) {
    ButtonGroup group = new ButtonGroup();
    Arrays.stream(buttons).forEach(group::add);
    return group;
  }

}
