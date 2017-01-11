package simulator;

import simulator.ui.Radix;

public interface Memory {
  default String[] toHexStringArray() {
    return toStringArray(Radix.HEX);
  }

  default String[] toDecimalStringArray() {
    return toStringArray(Radix.DECIMAL);
  }

  String[] toStringArray(Radix r);

  void resetMemory();
}
