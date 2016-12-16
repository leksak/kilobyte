package simulator;

public interface Observer<T> { // Package visible only - by design
  void notify(Observable<T> o);
}
