package kilobyte.simulator.hardware;

public class SignExtender {
  private static int MSB_INDEX = 15;

  public static int extend(int _16bitWord) {
    if (isBitSet(_16bitWord, MSB_INDEX)) {
      _16bitWord = setHigherOrderBits(unsetHigherOrderBits(_16bitWord));
    }

    return _16bitWord;
  }

  private static boolean isBitSet(int n, int bitIndex) {
    return ((n >>> bitIndex) & 0x1) == 1;
  }

  private static int unsetHigherOrderBits(int n) {
    return n & 0x0000ffff;
  }

  private static int setHigherOrderBits(int n) {
    return n | 0xffff0000;
  }
}
