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

  public void increment(int howMuch) {
    Add.add(howMuch, 4);
  }

  public void addObserver(Observer o) {

  }
}
