package simulator;

import java.util.Collection;


public interface Observable<T> {
  default void addObserver(Observer<T> o) {
    observers().add(o);
  }

  default void notifyObservers() {
    observers().forEach(o -> o.notify(this));
  }

  // Not particularly safe but considering the scale of the application
  // we will allow it.
  Collection<Observer<T>> observers();

  T reify();
}
