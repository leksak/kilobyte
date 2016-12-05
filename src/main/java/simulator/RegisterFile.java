package simulator;

import com.google.common.collect.ImmutableSet;
import common.hardware.Register;

import static common.hardware.Register.$t0;

public class RegisterFile {
  private ImmutableSet<Register> registers = ImmutableSet.copyOf(Register.values());

  private Register read(int address) {
    throw new UnsupportedOperationException();
  }

  public Register readRs(int rsAddress) {
    // Reads the rsRegister and fetches the data there
    throw new UnsupportedOperationException();
  }

  public Register readRt(int rtAddress) {
    throw new UnsupportedOperationException();
  }

  public void writeRd(int rdAddress, int value) {
    // Writes directly to the target register
  }
}
