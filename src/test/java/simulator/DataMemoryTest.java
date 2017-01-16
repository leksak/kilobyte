package simulator;

import common.machinecode.OperationsKt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import simulator.hardware.DataMemory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataMemoryTest {
  DataMemory dm;

  @BeforeEach
  public void setUp() {
    dm = new DataMemory();
  }

  @Test
  public void testReadByte() {
    byte expected = 5;
    dm.writeByteAt(1, expected);
    byte actual = dm.readByteFrom(1);
    assertEquals(actual, expected);
  }

  @Test
  public void testReadWord() {
    int _32bitWord = 0xf0f0f0f;
    dm.writeWordTo(4, _32bitWord);
    int _actual32bitWord = dm.readWordFrom(4);

    assertEquals(_32bitWord, _actual32bitWord);
  }

  @Test
  public void testNthByte() {
    int _32bitWord = 0xf0f0f0f;

    byte lowestByte = OperationsKt.nthByte(_32bitWord, 0);
    byte secondByte = OperationsKt.nthByte(_32bitWord, 1);
    byte thirdByte = OperationsKt.nthByte(_32bitWord, 2);
    byte fourthByte = OperationsKt.nthByte(_32bitWord, 3);

    assertTrue(lowestByte == 0x0f);
    assertTrue(secondByte == 0x0f);
    assertTrue(thirdByte == 0x0f);
    assertTrue(fourthByte == 0x0f);
  }

  @Test
  public void testReadBytes() {
    int _32bitWord = 0xf0f0f0f;

    dm.writeWordTo(4, _32bitWord);

    byte byte1 = dm.readByteFrom(7);
    byte byte2 = dm.readByteFrom(6);
    byte byte3 = dm.readByteFrom(5);
    byte byte4 = dm.readByteFrom(4);

    assertTrue(byte1 == 0x0f);
    assertTrue(byte2 == 0x0f);
    assertTrue(byte3 == 0x0f);
    assertTrue(byte4 == 0x0f);

  }
}
