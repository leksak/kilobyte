package common.instruction.exceptions;

public class NoSuchInstructionException extends Exception {
  public NoSuchInstructionException(String iname) {
    super(String.format("There is no instruction named: \"%s\"", iname));
  }
}
