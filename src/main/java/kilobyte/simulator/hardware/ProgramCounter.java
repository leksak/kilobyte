package kilobyte.simulator.hardware;

import lombok.Getter;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.java.Log;

import static java.lang.String.format;

@Value
@Log
@ToString
public class ProgramCounter {
  @NonFinal
  @Getter
  int addressPointer;

  private static final int INSTRUCTION_LENGTH_IN_NO_OF_BYTES = 4;

  public ProgramCounter() {
    /* The program counter always starts at 0 - always given in the number of bytes */
    addressPointer = 0;
  }

  public void stepForward() {
    log.info(format("Incrementing %s with %d bytes", this, INSTRUCTION_LENGTH_IN_NO_OF_BYTES));
    int prevAddress = addressPointer;
    addressPointer = addressPointer + 4; // TODO: This looks very weird
    log.info(format("PC={previousAddress=%d} PC={newAddress=%d}", prevAddress, addressPointer));
  }


  public int currentInstructionIndex() {
    return addressPointer / 4;
  }

  public void setTo(int addressInNoOfBytes) {
    log.info(format("Setting PC={addressPointer=%d} to PC={addressPointer=%d}", addressPointer, addressInNoOfBytes));
    addressPointer = addressInNoOfBytes;
  }

  public void setRelativeToCurrentAddress(int addressInNoOfBytes) {
    setTo(addressPointer + addressInNoOfBytes);
  }
}
