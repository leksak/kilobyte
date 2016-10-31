package common.instruction;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.expectThrows;

public class MnemonicRepresentationTests {
  static final String ANSI_GREEN = "\u001B[32m";
  static final String ANSI_RESET = "\u001B[0m";
  static final String ANSI_YELLOW = "\u001B[33m";
  static<T> void success(T arg) {
    System.out.println(ANSI_GREEN + "[SUCCESS] " + ANSI_YELLOW +
          "Exception was thrown as excepted: " + ANSI_RESET + arg);
  }
  
  @Test
  void testThatNopCannotBeInstantiatedWithTooManyArguments() {
    String faultyNopRepresentation = "nop foo";
    Throwable e = expectThrows(IllegalArgumentException.class, () ->
          new MnemonicRepresentation(faultyNopRepresentation));
    success(e.getMessage());
  }

  @Test
  void testThatNopCannotBeInstantiatedWithTooManyCommas() {
    String faultyNopRepresentation = "nop,";
    Throwable e = expectThrows(IllegalArgumentException.class, () ->
          new MnemonicRepresentation(faultyNopRepresentation));
    success(e.getMessage());
  }

  @Test
  void testThatAddCannotBeInstantiatedWithTooManyArguments() {
    String faultyAddRepresentation = "add $t1, $t2, $t3, $t1";
    Throwable e = expectThrows(IllegalArgumentException.class, () ->
          new MnemonicRepresentation(faultyAddRepresentation));
    success(e.getMessage());
  }

  @Test
  void testThatAddCannotContainParentheses() {
    String faultyAddRepresentation = "add $t1, $t2, $t3()";
    Throwable e = expectThrows(IllegalArgumentException.class, () ->
          new MnemonicRepresentation(faultyAddRepresentation));
    success(e.getMessage());
  }

  @Test
  void testThatWhiteSpaceBetweenArgumentsDoNotMatter() throws Exception {
    new MnemonicRepresentation("add $t1,$t2,$t3");
  }

  @Test
  void testThatExceptionIsThrownWhenThereAreIllegalCharacters() throws Exception {
    Throwable e = expectThrows(IllegalArgumentException.class, () ->
          new MnemonicRepresentation("add $t1, $t2, $t3!#($sp)"));
    success(e.getMessage());
  }

  @Test
  void testThatAddCannotBeInstantiatedWithTooFewArguments() {
    String faultyAddRepresentation = "add $t1, $t2";
    Throwable e = expectThrows(IllegalArgumentException.class, () ->
          new MnemonicRepresentation(faultyAddRepresentation));
    success(e.getMessage());
  }

  @Test
  void testThatAddCannotBeInstantiatedWithTooManyCommas() {
    String faultyAddRepresentation = "add $t1, $t2,, $t3";
    Throwable e = expectThrows(IllegalArgumentException.class, () ->
          new MnemonicRepresentation(faultyAddRepresentation));
    success(e.getMessage());
  }

  @Test
  void testThatAddCannotBeInstantiatedWithTooFewCommas() {
    String faultyAddRepresentation = "add $t1, $t2 $t3";
    Throwable e = expectThrows(IllegalArgumentException.class, () ->
          new MnemonicRepresentation(faultyAddRepresentation));
    success(e.getMessage());
  }
}
