package common.instruction;

public enum Pattern {
  INAME_RD_RS_RT(3),
  NOP(0),
  ;

  public final int expectedNumberOfArguments;

  Pattern(int expectedNumberOfArguments) {
    this.expectedNumberOfArguments = expectedNumberOfArguments;
  }

  boolean correctNumberOfArguments(int actualNumberOfArguments) {
    return actualNumberOfArguments == expectedNumberOfArguments;
  }
}
