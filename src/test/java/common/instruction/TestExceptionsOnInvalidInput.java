package common.instruction;

import common.instruction.exceptions.IllegalCharactersInMnemonicException;
import common.instruction.exceptions.NoSuchInstructionException;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.StringJoiner;

import static org.junit.jupiter.api.Assertions.expectThrows;

public class TestExceptionsOnInvalidInput {
  static final String ANSI_GREEN = "\u001B[32m";
  static final String ANSI_RESET = "\u001B[0m";
  static final String ANSI_YELLOW = "\u001B[33m";
  static final String ANSI_BLUE = "\u001B[34m";
  static final String LBR = "\u001B[1;34m[" + ANSI_RESET;
  static final String RBR = "\u001B[1;34m]" + ANSI_RESET;

  static final String SUCCESS = ANSI_GREEN + "[SUCCESS]" + ANSI_RESET;

  static String yellow(String s) {
    return ANSI_YELLOW + s + ANSI_RESET;
  }
  static String blue(String s) {
    return ANSI_BLUE + s + ANSI_RESET;
  }

  static String format(String... args) {
    StringJoiner sj = new StringJoiner(" ");
    for (String arg : args) {
      sj.add(arg);
    }
    return sj.toString();
  }

  static void success(String message, String what, String it) {
    String s = format(
          SUCCESS, yellow(message), blue(what) + LBR + it + RBR);
    System.out.println(s);
  }

  static void success(String message, String mnemonic) {
    success(message, "mnemonic=", mnemonic);
  }

  static void success(Throwable e) {
    success("Exception was thrown as expected:", "e.getMessage=", e.getMessage());
  }

  @Test
  void testThatNopCannotBeInstantiatedWithTooManyArguments() {
    /*String faultyNopRepresentation = "nop foo";
    Throwable e = expectThrows(IllegalArgumentException.class, () ->
          Instruction.from(faultyNopRepresentation));
    success(e);*/
  }

  @Test
  void testThatNopCannotBeInstantiatedWithTrailingCharacters() {
    String faultyNopRepresentation = "nop,";
    Throwable e = expectThrows(IllegalArgumentException.class, () ->
          Instruction.from(faultyNopRepresentation));
    success(e);
  }

  @Test
  void testThatAddCannotBeInstantiatedWithTooManyArguments() {
    String faultyAddRepresentation = "add $t1, $t2, $t3, $t1";
    Throwable e = expectThrows(IllegalArgumentException.class, () ->
          Instruction.from(faultyAddRepresentation));
    success(e);
  }

  @Test
  void testThatAddCannotContainParentheses() {
    String faultyAddRepresentation = "add $t1, $t2, $t3()";
    Throwable e = expectThrows(IllegalArgumentException.class, () ->
          Instruction.from(faultyAddRepresentation));
    success(e);
  }

  @Test
  void testThatExceptionIsThrownWhenThereAreIllegalCharacters() throws Exception {
    Throwable e = expectThrows(IllegalCharactersInMnemonicException.class, () ->
          Instruction.from("add $t1, $t2, $t3!#$sp"));
    success(e);
  }

  @Test
  void testThatWhiteSpaceBetweenArgumentsDoNotMatter() throws Exception {
    String mnemonicWithoutWhitespace = "add $t1,$t2,$t3";
    Instruction.from(mnemonicWithoutWhitespace);
    success("No exception was caused by a lack of whitespace:",
          mnemonicWithoutWhitespace);
  }

  @Test
  void testThatAddCannotBeInstantiatedWithTooFewArguments() {
    String faultyAddRepresentation = "add $t1, $t2";
    Throwable e = expectThrows(IllegalArgumentException.class, () ->
          Instruction.from(faultyAddRepresentation));
    success(e);
  }

  @Test
  void testThatAddCannotBeInstantiatedWithTooManyCommas() {
    String faultyAddRepresentation = "add $t1, $t2,, $t3";
    Throwable e = expectThrows(IllegalArgumentException.class, () ->
          Instruction.from(faultyAddRepresentation));
    success(e);
  }

  @Test
  void testThatAddCannotBeInstantiatedWithTooFewCommas() {
    String faultyAddRepresentation = "add $t1, $t2 $t3";
    Throwable e = expectThrows(IllegalArgumentException.class, () ->
          Instruction.from(faultyAddRepresentation));
    success(e);
  }
}
