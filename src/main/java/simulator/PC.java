package simulator;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.LinkedList;
import java.util.List;

@Value
public class PC {
  @NonFinal
  @Getter
  int currentAddress;

  public PC() {
    currentAddress = 0;
  }

  public void increment(int howMuchInNumberOfBytes) {
    Add.add(howMuchInNumberOfBytes, 4);
  }
}
