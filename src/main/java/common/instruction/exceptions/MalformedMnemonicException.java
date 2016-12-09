package common.instruction.exceptions;

public class MalformedMnemonicException extends RuntimeException {
  MalformedMnemonicException(String message) {
    super(message);
  }

  public MalformedMnemonicException(String mnemonic, String message) {
    this("Mnemonic: \"" + mnemonic + "\" is malformed. Cause: " + message);
  }
}
