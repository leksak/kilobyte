package common.instruction;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.expectThrows;

public class MnemonicRepresentationTests {
  @Test
  void testThatNopCannotBeInstantiatedWithTooManyArguments() {
    String faultyNopRepresentation = "nop foo";
    Throwable e = expectThrows(IllegalArgumentException.class, () ->
          new MnemonicRepresentation(faultyNopRepresentation));
    System.out.println(e.getMessage());
  }

  @Test
  void testThatNopCannotBeInstantiatedWithTooManyCommas() {
    String faultyNopRepresentation = "nop,";
    Throwable e = expectThrows(IllegalArgumentException.class, () ->
          new MnemonicRepresentation(faultyNopRepresentation));
    System.out.println(e.getMessage());
  }

  @Test
  void testThatAddCannotBeInstantiatedWithTooManyArguments() {
    String faultyAddRepresentation = "add $t1, $t2, $t3, $t1";
    Throwable e = expectThrows(IllegalArgumentException.class, () ->
          new MnemonicRepresentation(faultyAddRepresentation));
    System.out.println(e.getMessage());
  }

  @Test
  void testThatAddCannotBeInstantiatedWithTooFewArguments() {
    String faultyAddRepresentation = "add $t1, $t2";
    Throwable e = expectThrows(IllegalArgumentException.class, () ->
          new MnemonicRepresentation(faultyAddRepresentation));
    System.out.println(e.getMessage());
  }

  @Test
  void testThatAddCannotBeInstantiatedWithTooManyCommas() {
    String faultyAddRepresentation = "add $t1, $t2,, $t3";
    Throwable e = expectThrows(IllegalArgumentException.class, () ->
          new MnemonicRepresentation(faultyAddRepresentation));
    System.out.println(e.getMessage());
  }

  @Test
  void testThatAddCannotBeInstantiatedWithTooFewCommas() {
    String faultyAddRepresentation = "add $t1, $t2 $t3";
    Throwable e = expectThrows(IllegalArgumentException.class, () ->
          new MnemonicRepresentation(faultyAddRepresentation));
    System.out.println(e.getMessage());
  }
}
