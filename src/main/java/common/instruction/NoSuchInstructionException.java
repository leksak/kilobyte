package common.instruction;

public class NoSuchInstructionException extends Exception {
  public NoSuchInstructionException(String iname) {
    super(String.format("There is no instruction named: \"%s\"", iname));
  }

  public NoSuchInstructionException(int machineCode) {
    super(String.valueOf(machineCode));
  }

  public NoSuchInstructionException(String formatString, Object... args) {
    super(String.format(formatString, args));
  }
}
