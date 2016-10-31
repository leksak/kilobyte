package common.instruction;

import com.google.common.base.Preconditions;

import static com.google.common.base.Preconditions.checkArgument;

public enum Pattern {
  INAME_RD_RS_RT(3),
  NOP(0),
  ;

  public final int expectedNumberOfArguments;

  Pattern(int expectedNumberOfArguments) {
    this.expectedNumberOfArguments = expectedNumberOfArguments;
  }

  boolean correctNumberOfArguments(String... args) {
    return correctNumberOfArguments(args.length);
  }

  boolean correctNumberOfArguments(int actualNumberOfArguments) {
    return actualNumberOfArguments == expectedNumberOfArguments;
  }
}
