package simulator;

import java.util.Collection;

public interface Observable<T> {
  void addObserver(Observer<T> o);
  Collection<Observer<T>> observers();

  default void update() {
    observers().forEach(o -> o.notify(this));
  }
}
