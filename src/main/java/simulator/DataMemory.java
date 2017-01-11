package simulator;

import org.apache.commons.lang3.ArrayUtils;
import simulator.ui.Radix;

public class DataMemory implements Memory {
  private Byte[] memory = ArrayUtils.toObject(new byte[1000]);

  @Override
  public String[] toStringArray(Radix r) {
    String[] d = new String[memory.length];
    for (int i = 0; i < d.length; i++) {
      if (r == Radix.HEX) {
        d[i] = "0x" + Integer.toHexString(memory[i]);
      } else if (r == Radix.DECIMAL) {
        d[i] = Integer.toString(memory[i]);
      }
    }

    return d;
  }

  @Override
  public void resetMemory() {
    memory = ArrayUtils.toObject(new byte[1000]);
  }
}
