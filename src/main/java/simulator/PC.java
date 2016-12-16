package simulator;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.*;

@Value
public class PC implements Observable<PC> {
  List<Observer<PC>> observers = new LinkedList<>();

  @NonFinal
  @Getter
  int currentAddress;

  public PC() {
    currentAddress = 0;
  }

  public void increment(int howMuch) {
    Add.add(howMuch, 4);
  }

  @Override
  public void addObserver(Observer<PC> o) {
    observers.add(o);
  }

  @Override
  public ImmutableCollection<Observer<PC>> observers() {
    return ImmutableList.copyOf(observers);
  }

  @Override
  public PC reify() {
    return this;
  }
}
