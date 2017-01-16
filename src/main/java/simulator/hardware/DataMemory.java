package simulator.hardware;

import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.java.Log;
import simulator.ui.utils.Radix;

import java.nio.ByteBuffer;
import static java.lang.String.format;

@Value
@Log
@NoArgsConstructor
public class DataMemory implements Memory {
  int NO_OF_BYTES = 1000;

  @NonFinal
  ByteBuffer memory = ByteBuffer.allocate(NO_OF_BYTES);

  @Override
  public String[] toStringArray(Radix r) {
    String[] d = new String[250];
    for (int i = 0; i < d.length; i++) {
      if (r == Radix.HEX) {
        d[i] = "0x" + Integer.toHexString(memory.get(i));
      } else if (r == Radix.DECIMAL) {
        d[i] = Integer.toString(memory.get(i));
      }
    }

    return d;
  }

  @Override
  public void resetMemory() {
    memory = ByteBuffer.allocate(NO_OF_BYTES);
  }

  public void writeByteAt(int index, Byte value) {
    memory.put(index, value);
  }

  public byte readByteFrom(int byteAddress) {
    return memory.get(byteAddress);
  }

  /* Read from the n:th byte and forward */
  public int readWordFrom(int byteAddress) {
    int word = memory.getInt(byteAddress);
    return word;
  }

  /**
   * Writes a word with the highest byte being stored at the specified byte
   * address, i.e. consider the memory
   *
   * +-------------+
   * | Byte Addr=0 | <- This block is 4 bytes, viz. 32-bits
   * +-------------+
   * | Byte Addr=4 |
   * +-------------+
   * | Byte Addr=8 |
   * +-------------+
   * | ........... |
   *
   * Hence we have that the Byte Addr=0 block can be visualized
   * as follows,
   *
   * +-------+-------+-------+-----+
   * | 31:24 | 23:16 | 15:8  | 7:0 | (bits)
   * +-------+-------+-------+-----+
   *     ^                      ^
   *     |                      |
   *   4th byte             1st byte
   *
   * Let x = 0b0000 1111 0000 1111 0000 1111 0000 1111 = 0xf0f0f0f
   * then we'd get that
   *
   * dm.writeWordTo(4, x);
   *
   * would result in the memory
   * +------------+------------+------------+----------+
   * | 31:24=0x00 | 23:16=0x00 | 15:8=0x00  | 7:0=0x00 | Byte addr=0
   * +------------+------------+------------+----------+
   * | 31:24=0x0f | 23:16=0x0f | 15:8=0x0f  | 7:0=0x0f | Byte addr=4
   * +------------+------------+------------+----------+
   *
   * @param byteAddress
   * @param bitWord
   */
  public void writeWordTo(int byteAddress, int bitWord) {
    memory.putInt(byteAddress, bitWord);
  }
}
