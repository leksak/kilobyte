package common.instruction.mnemonic;

import common.instruction.NoSuchInstructionException;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.expectThrows;

public class MnemonicRepresentationTests {
  static final String ANSI_GREEN = "\u001B[32m";
  static final String ANSI_RESET = "\u001B[0m";
  static final String ANSI_YELLOW = "\u001B[33m";
  static void success(Throwable e) {
    System.out.println(ANSI_GREEN + "[SUCCESS] " + ANSI_YELLOW +
          "Exception was thrown as excepted: " + ANSI_RESET + e);
  }
  
  static void success(String msg, String... args) {
    System.out.println(ANSI_GREEN + "[SUCCESS] " + ANSI_YELLOW +
          msg + ANSI_RESET + Arrays.toString(args));
  }
  
  @Test
  void testThatNopCannotBeInstantiatedWithTooManyArguments() {
    String faultyNopRepresentation = "nop foo";
    Throwable e = expectThrows(IllegalArgumentException.class, () ->
          MnemonicRepresentation.fromString(faultyNopRepresentation));
    success(e);
  }

  @Test
  void testThatNopCannotBeInstantiatedWithTrailingCharacters() {
    String faultyNopRepresentation = "nop,";
    // There is no instruction named "nop,"
    Throwable e = expectThrows(NoSuchInstructionException.class, () ->
          MnemonicRepresentation.fromString(faultyNopRepresentation));
    success(e);
  }

  @Test
  void testThatAddCannotBeInstantiatedWithTooManyArguments() {
    String faultyAddRepresentation = "add $t1, $t2, $t3, $t1";
    Throwable e = expectThrows(IllegalArgumentException.class, () ->
          MnemonicRepresentation.fromString(faultyAddRepresentation));
    success(e);
  }

  @Test
  void testThatAddCannotContainParentheses() {
    String faultyAddRepresentation = "add $t1, $t2, $t3()";
    Throwable e = expectThrows(IllegalArgumentException.class, () ->
          MnemonicRepresentation.fromString(faultyAddRepresentation));
    success(e);
  }

  @Test
  void testThatWhiteSpaceBetweenArgumentsDoNotMatter() throws Exception {
    val mnemonicWithoutWhitespace = "add $t1,$t2,$t3";
    MnemonicRepresentation.fromString(mnemonicWithoutWhitespace);
    success("Instantiating a mnemonic from a string lacking" +
          " whitespaces between args does not cause an exception: ", mnemonicWithoutWhitespace);

  }

  @Test
  void testThatExceptionIsThrownWhenThereAreIllegalCharacters() throws Exception {
    Throwable e = expectThrows(IllegalArgumentException.class, () ->
          MnemonicRepresentation.fromString("add $t1, $t2, $t3!#($sp)"));
    success(e);
  }

  @Test
  void testThatAddCannotBeInstantiatedWithTooFewArguments() {
    String faultyAddRepresentation = "add $t1, $t2";
    Throwable e = expectThrows(IllegalArgumentException.class, () ->
          MnemonicRepresentation.fromString(faultyAddRepresentation));
    success(e);
  }

  @Test
  void testThatAddCannotBeInstantiatedWithTooManyCommas() {
    String faultyAddRepresentation = "add $t1, $t2,, $t3";
    Throwable e = expectThrows(IllegalArgumentException.class, () ->
          MnemonicRepresentation.fromString(faultyAddRepresentation));
    success(e);
  }

  @Test
  void testThatAddCannotBeInstantiatedWithTooFewCommas() {
    String faultyAddRepresentation = "add $t1, $t2 $t3";
    Throwable e = expectThrows(IllegalArgumentException.class, () ->
          MnemonicRepresentation.fromString(faultyAddRepresentation));
    success(e);
  }
}
