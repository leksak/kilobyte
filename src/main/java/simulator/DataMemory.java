package simulator;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

public class DataMemory implements Memory {
  private Byte[] memory = ArrayUtils.toObject(new byte[1000]);

  @Override
  public String[] displayMemoryContents() {
    String[] d = new String[memory.length];
    for (int i = 0; i < d.length; i++) {
      d[i] = memory[i].toString();
    }
    return d;
  }

  @Override
  public void resetMemory() {
    memory = ArrayUtils.toObject(new byte[1000]);
  }
}
