package common.instruction;

public enum MnemonicPattern {
  INAME_RD_RS_RT(3),
  NOP(0),
  ;

  public final int expectedNumberOfArguments;

  MnemonicPattern(int expectedNumberOfArguments) {
    this.expectedNumberOfArguments = expectedNumberOfArguments;
  }

  boolean correctNumberOfArguments(int actualNumberOfArguments) {
    return actualNumberOfArguments == expectedNumberOfArguments;
  }
}
