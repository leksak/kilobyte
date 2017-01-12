package simulator;

import lombok.Getter;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
public class PC {
  @NonFinal
  @Getter
  int currentAddress;

  public PC() {
    currentAddress = 0;
  }

  public void increment(int howMuchInNumberOfBytes) {
    System.err.println("PC Before:"+currentAddress+" After:"+Add.add(howMuchInNumberOfBytes, 4));
    currentAddress += Add.add(howMuchInNumberOfBytes, 4);
  }

  public void stepForward() {
    currentAddress += 32;
  }

}
