package simulator.hardware;

import lombok.Getter;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.java.Log;
import simulator.Add;

import static java.lang.String.*;

@Value
@Log
@ToString
public class PC {
  @NonFinal
  @Getter
  int currentAddress;

  public PC() {
    /* The program counter always starts at 0 - always given in the number of bytes */
    currentAddress = 0;
  }

  private void increment(int noOfBytes) {
    log.info(format("Incrementing %s with %d number of bytes", this, noOfBytes));
    int prevAddress = currentAddress;
    currentAddress += Add.add(currentAddress, noOfBytes);
    System.err.println("PC Before:"+prevAddress+" After:"+ currentAddress);
  }

  public void stepForward() {
    increment(4);
  }

  public void setTo(int addressInNoOfBytes) {
    log.info(format("Setting PC={currentAddress=%d} to PC={currentAddress=%d}", currentAddress, addressInNoOfBytes));
    currentAddress = addressInNoOfBytes;
  }
}
