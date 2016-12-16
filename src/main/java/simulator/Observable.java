package simulator;

import com.google.common.collect.ImmutableCollection;

import java.util.Collection;

public interface Observable<T> {
  void addObserver(Observer<T> o);

  default void notifyObservers() {
    observers().forEach(o -> o.notify(this));
  }

  ImmutableCollection<Observer<T>> observers();

  T reify();
}
