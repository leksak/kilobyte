package common.instruction.exceptions;

import java.util.StringJoiner;

public class IllegalCharactersInMnemonicException extends MalformedMnemonicException {
  private static final String formatString = "Illegal character(s) encountered: illegal(s)=%s in \"%s\"";

  private IllegalCharactersInMnemonicException(String message) {
    super(message);
  }

  public IllegalCharactersInMnemonicException(String mnemonic, String illegalCharacters) {
    this(String.format(formatString, illegalCharacters, mnemonic));
  }

  public IllegalCharactersInMnemonicException(String mnemonic, StringJoiner illegalCharacters) {
    this(mnemonic, illegalCharacters.toString());
  }
}
