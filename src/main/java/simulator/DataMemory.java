package simulator;

import org.apache.commons.lang3.ArrayUtils;
import lombok.extern.java.Log;
import simulator.ui.Radix;

import java.nio.ByteBuffer;

import static java.lang.String.format;
@Log
public class DataMemory implements Memory {
  private ByteBuffer memory = ByteBuffer.allocate(1000);

  @Override
  public String[] toStringArray(Radix r) {
    //TODO:
    /*
    String[] d = new String[memory.le];
    for (int i = 0; i < d.length; i++) {
      if (r == Radix.HEX) {
        d[i] = "0x" + Integer.toHexString(memory[i]);
      } else if (r == Radix.DECIMAL) {
        d[i] = Integer.toString(memory[i]);
      }
    }


    return d;
    */
    return new String[1];
  }

  @Override
  public void resetMemory() {
    memory.reset();
  }

  public void setMemory(int index, Byte value) {
    log.info(format(
          "Put DataMemory={address=%d, byteValue=(hex=0x%02x, int=%d).",
          index,
          value,
          value.intValue()));
    memory.put(index, value);
  }

  public int readWord(int address) {
    log.info(format(
          "Fetching DataMemory={address=%d, byteValue=(hex=0x%02x, int=%d).",
          address,
          memory.get(address),
          memory.get(address)));

    return memory.get(address);
  }
}
