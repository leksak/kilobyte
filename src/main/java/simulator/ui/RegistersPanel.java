package simulator.ui;

import common.hardware.RegisterFile;
import lombok.Value;

@Value
public class RegistersPanel {
  RegisterFile rf;

  public RegistersPanel(RegisterFile rf) {
    this.rf = rf;
  }
}
