package common.instruction;

import common.hardware.Register;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MnemonicRepresentation {
  private static String standardizeMnemonic(String mnemonic) {
    // Begin by replacing all commas with a space,
    // thereby transforming:
    //
    // add $t1,$t2, $t3 (intentional space before $t3)
    //
    // so we get
    //
    // add $t1 $t2  $t3 (double space before $t3)
    //
    // Then, replace all white-space characters (\\s+) with a single
    // space and remove any leading or trailing spaces (trim).
    //
    // This would standardize both "add $t1, $t2, $t3" and
    // "    add $t1,$t2,  $t3  " to the same string, namely
    // "add $t1 $t2 $t3". This sequence of operations also standardizes
    // "jr $t1" to "jr $t1" (identity transformation).
    return mnemonic.replaceAll(",", " ")
          .replaceAll("\\s+", " ")
          .trim();
  }

  public String iname() {
    return iname;
  }
  public String[] args() {
    return args;
  }

  private final String mnemonic;

  // Might be empty for certain instructions, for an example "nop"
  // TODO: should use optional? Testing will decide.
  private final String[] args;
  private final String iname;

  public MnemonicRepresentation(String iname, String... args) {
    this.iname = iname;

    String mnemonic = iname;
    if (args.length > 0) {
      mnemonic += " ";

      StringJoiner sj = new StringJoiner(", ");
      Arrays.stream(args).forEach(sj::add);
      mnemonic += sj.toString();
    }

    this.mnemonic = mnemonic;
    this.args = args;
  }

  private void throwExceptionIfContainsNewLineCharacter(String mnemonic) {
    String newline = System.getProperty("line.separator");
    if (mnemonic.contains(newline)) {
      val s = "Illegal character(s) encountered: '<newline>'";
      throw new IllegalArgumentException(s + " in \"" + mnemonic + "\"");
    }
  }

  private void throwExceptionIfContainsIllegalCharacters(String mnemonic) {
    throwExceptionIfContainsNewLineCharacter(mnemonic);

    // Check for other illegal characters:
    // goo.gl/Q8EiLb
    String regex = "[A-Za-z, \\\\(\\\\)\\\\$0-9]";
    Pattern p = Pattern.compile(regex);
    Matcher m = p.matcher(mnemonic);

    String mask = m.replaceAll("+").replaceAll("[^+]", "-");

    if (mask.contains("-")) {
      // At least one illegal character was detected,
      val illegalCharacters = new StringJoiner("', '", "['", "']");
      for (int i = 0; i < mnemonic.length(); i++) {
        if (mask.charAt(i) == '-') {
          illegalCharacters.add(String.valueOf(mnemonic.charAt(i)));
        }
      }
      val s = "Illegal character(s) encountered: " + illegalCharacters.toString();
      throw new IllegalArgumentException(s + " in \"" + mnemonic + "\"");
    }
  }

  public MnemonicRepresentation(String mnemonic)
        throws NoSuchInstructionException {
    throwExceptionIfContainsIllegalCharacters(mnemonic);

    String standardized = standardizeMnemonic(mnemonic);
    String[] tokens = standardized.split(" ");
    iname = tokens[0];
    int expectedNumberOfCommas;

    if (tokens.length > 1) { // Not "nop", "exit", etc.
      MnemonicPattern p = Instruction.getPattern(iname);

      args = Arrays.copyOfRange(tokens,
            1, // Ignore the iname
            tokens.length);

      // Count the number of arguments
      int argc = args.length;
      expectedNumberOfCommas = argc - 1;

      if (!p.correctNumberOfArguments(argc)) {
        // There was too many arguments
        val err = String.format("\"%s\": Expected %d arguments. Got: %d",
              mnemonic, p.expectedNumberOfArguments, argc);
        throw new IllegalArgumentException("Wrong number of arguments: " + err);
      }
    } else {
      args = new String[0];
      expectedNumberOfCommas = 0;
    }

    // There should be as many commas as there are arguments - 1,
    // For an example "add $t1 $t2 $t3" has 3 arguments ($t1, $t2, $t3)
    // and there should be 2 commas. For "jr $t1" there is one
    // argument, so there should be zero commas.
    val actualNumberOfCommas = StringUtils.countMatches(mnemonic, ",");

    if (actualNumberOfCommas != expectedNumberOfCommas) {
      // TODO: Document missing commas, or possibly too many
      // TODO: i.e. "jr $t1," should not have a trailing comma
      val actual = actualNumberOfCommas;
      val expected = expectedNumberOfCommas;
      String err = String.format("\"%s\". Expected: %d. Got: %d",
            mnemonic, expected, actual);
      throw new IllegalArgumentException("Wrong number of commas: " + err);
    }

    StringJoiner sj = new StringJoiner(", ");

    for (String arg : args) {
      sj.add(arg);
    }

    this.mnemonic = iname + " " + sj.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MnemonicRepresentation mnemonic1 = (MnemonicRepresentation) o;

    if (!iname.equals(mnemonic1.iname)) {
      // If the names mismatch any additional comparisons are
      // point-less.
      return false;
    }

    if (args != null && mnemonic1.args != null) {
      if (args.length != mnemonic1.args.length) {
        return false;
      } else {
        for (int i = 0; i < args.length; i++) {
          /* Check that the _arguments_ are semantically
           * the same, i.e. "$8" is the same as "$t0".
           * Symbolic and non-symbolic references to
           * registers both start with "$" so we utilize
           * that knowledge to compare the arguments seeing
           * as there might be arguments that are _not_
           * references to registers, such as a shift amount.
           *
           * Since the inames are the same, we assume that
           * the arguments appear in the same order and
           * adhere to the appropriate pattern.
           *
           * TODO: This is not battle-tested
           */
          String ours = this.args[i];
          String theirs = mnemonic1.args[i];

          if (ours.startsWith("$")) {
            if (Register.fromString(ours) !=
                  Register.fromString(theirs)) {
              return false;
            }
          } else {
            // A non-register field, possibly shamt.
            if (!MachineCodeDecoder.equals(ours, theirs)) {
              return false;
            }
          }
        }
      }
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = mnemonic.hashCode();
    result = 31 * result + Arrays.hashCode(args);
    result = 31 * result + iname.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return mnemonic;
  }
}
