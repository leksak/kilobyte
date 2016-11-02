package common.instruction.exceptions;

import lombok.val;

import java.util.StringJoiner;

public class IllegalCharactersInMnemonicException extends IllegalArgumentException {
  private static final String formatString = "Illegal character(s) encountered: %s in \"%s\"";

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
