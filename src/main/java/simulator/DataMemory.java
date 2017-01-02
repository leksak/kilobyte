package simulator;

import lombok.Value;
import org.apache.commons.lang3.ArrayUtils;
public class DataMemory implements Memory<Byte> {
  private Byte[] memory = ArrayUtils.toObject(new byte[1000]);

  @Override
  public Byte[] getMemoryContents() {
    return memory;
  }

  @Override
  public void resetMemory() {
    memory = ArrayUtils.toObject(new byte[1000]);
  }
}
