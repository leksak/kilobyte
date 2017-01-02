package simulator;

public interface Memory<T> {
  T[] getMemoryContents();
  void resetMemory();
}
