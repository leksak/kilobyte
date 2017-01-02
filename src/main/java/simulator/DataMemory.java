package simulator;

import lombok.Value;
import org.apache.commons.lang3.ArrayUtils;

@Value
public class DataMemory implements Memory<Byte> {
  Byte[] memory = ArrayUtils.toObject(new byte[1000]);

  @Override
  public Byte[] getMemoryContents() {
    return memory;
  }
}
