package simulator;

import lombok.Getter;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Value
public class PC implements Observable<PC> {
  Set<Observer<PC>> observers = new HashSet<>();

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
  public Collection<Observer<PC>> observers() {
    return observers;
  }
}
