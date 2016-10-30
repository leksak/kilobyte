package common.instruction;

public class NoSuchInstructionException extends Exception {
  public NoSuchInstructionException(String s) {
    super(s);
  }

  public NoSuchInstructionException(String formatString, Object... args) {
    this(String.format(formatString, args));
  }
}
