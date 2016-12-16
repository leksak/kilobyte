package simulator;

public interface Observer<T> {
  void notify(Observable<T> o);
}
